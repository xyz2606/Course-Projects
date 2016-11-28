#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <stddef.h>
#include <assert.h>
#include <poll.h>
#include <errno.h>
#include <time.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <sys/uio.h>
#include <netinet/in.h>

#include "rlib.h"

struct vector_pkt {// a vector class just like vector<packet_t> in C++! 
	packet_t ** p;
	int reserved;
	int len;
};
struct vector_pkt * new_vector_pkt() {// allocate a new vector_pkt!
	struct vector_pkt * vec = malloc(sizeof(struct vector_pkt));
	vec->len = 0;
	vec->p = malloc(10 * sizeof(packet_t * ));
	vec->reserved = 10;
	return vec;
}
void push_back_pkt(struct vector_pkt * vec, packet_t * pkt) {// just like vector<char>::push_back() in C++!
	if(vec->len == vec->reserved) {
		packet_t ** new_p = malloc(vec->reserved * 2 * sizeof(packet_t *));
		memcpy(new_p, vec->p, vec->reserved * sizeof(packet_t *));
		free(vec->p);
		vec->p = new_p;
		vec->reserved *= 2;
	}
	(vec->p)[vec->len] = pkt;
	vec->len++;
}
struct reliable_state {
	rel_t * next;			/* Linked list for traversing all connections */
	rel_t ** prev;

	conn_t  *c;			/* This is the connection object */

	/* Add your own data fields below this */
	int window_size;//window size of the sending and receiving window
	int time_out;//used to resend unacked packets
	int time;//time now, increased by rel_timer()
	/*destination*/
	struct sockaddr_storage * dest;//destination of the connection

	/*Sender State*/
	struct vector_pkt * send_pkt_vec;//a vector of packets sent/to be send
	int send_unack;//the first unacked pkt
	int send_stamp;//send time of the pkt above
	int send_unsent;//the first unsent pkt
	/*Receiver State*/
	struct vector_pkt * recv_pkt_vec;//a vector of packets received
	int recv_lastack;//last ackno sent
	int recv_output_pkt;//the packet being output now
};

rel_t * rel_list;

/* Creates a new reliable protocol session, returns NULL on failure.
 * Exactly one of c and ss should be NULL.  (ss is NULL when called
 * from rlib.c, while c is NULL when this function is called from
 * rel_demux.) */
rel_t * rel_create(conn_t * c, const struct sockaddr_storage * ss, const struct config_common * cc) {
	rel_t *r;
	r = xmalloc(sizeof(*r));
	memset(r, 0, sizeof(*r));

	if(!c) {
		c = conn_create(r, ss);
		if(!c) {
			free(r);
			return NULL;
		}
	}

	r->c = c;
	r->next = rel_list;
	r->prev = &rel_list;
	if(rel_list) {
		rel_list->prev = &r->next;
	}
	rel_list = r;

	/* Do any other initialization you need here */

	r->window_size = cc->window;//initialize the window size
	r->time_out = 5;//or this can be cc->timeout / cc->timer; but 5 is also right.
	if(r->time_out <= 0) {
		r->time_out = 1;
	}
	r->time = 0;
	if(ss != NULL) {//if this is not a single connection(lab 1)
		r->dest = malloc(sizeof(struct sockaddr_storage));
		memcpy(r->dest, ss, sizeof(struct sockaddr_storage));
	}
	r->send_pkt_vec = new_vector_pkt();//initializations
	r->send_unack = 0;
	r->send_stamp = -1;
	r->send_unsent = 0;
	r->recv_pkt_vec = new_vector_pkt();
	r->recv_output_pkt = 0;
	r->recv_lastack = 1;
	return r;
}

void rel_destroy(rel_t * r) { 
	/* destroy the rel_t. But it seems not required and you can delete the codes in this function! */
	if (r->next)
		r->next->prev = r->prev;
	*r->prev = r->next;
	conn_destroy (r->c);

	/* Free any other allocated memory here */
	
	free(r->send_pkt_vec->p);// free the spaces reserved for the two vectors.
	free(r->recv_pkt_vec->p);
	if(r->dest) {// free the spaces for the destination storage
		free(r->dest);
	}
}

/* This function only gets called when the process is running as a
 * server and must handle connections from multiple clients.  You have
 * to look up the rel_t structure based on the address in the
 * sockaddr_storage passed in.  If this is a new connection (sequence
 * number 1), you will need to allocate a new conn_t using rel_create
 * ().  (Pass rel_create NULL for the conn_t, so it will know to
 * allocate a new connection.)
 */
void rel_demux(const struct config_common * cc, const struct sockaddr_storage * ss, packet_t * pkt, size_t len) {
	/* simply walk through the linked list to find which connection should be use */
	rel_t * p = rel_list;
	while(p != NULL) {
		if(addreq(p->dest, ss)) {
			rel_recvpkt(p, pkt, len);
			return;
		}
		p = p->next;
	}
	rel_create(NULL, ss, cc);//a new connection!
	rel_recvpkt(rel_list, pkt, len);
}

void send_pkt(rel_t * r, packet_t * p) {
	/* send a packet */
	p->ackno = htonl(r->recv_lastack);// ackno is unnessisary for the program to work correctly since independent ack packets are sent.
	p->cksum = 0;
	p->cksum = cksum(p, ntohs(p->len));
	conn_sendpkt(r->c, p, ntohs(p->len));
	if(ntohl(p->seqno) - 1 == r->send_unack) {//when sending the first packet in the window, reset the sending time
		r->send_stamp = r->time;
	}
}

void try_send(rel_t * r) {
	/* send packets in the window */
	int i;
	for(i = r->send_unsent; i < r->send_pkt_vec->len && i < r->send_unack + (r->send_pkt_vec->p[r->send_unack]->len == htons(512) ? r->window_size : 1); i++) {// if a not-full packet is not acked, the window size is restricted to 1.
		send_pkt(r, r->send_pkt_vec->p[i]);
		r->send_unsent ++;
	}
}

void try_output(rel_t * r);

void try_ack(rel_t * r, int ackno);

void try_destroy(rel_t * r) {
	/* check for whether the connection can be destroyed. */
	//the 4 conditions in the pdf can be described as 2 conditions: your EOF has been acked and you has output the other's EOF.
	if(r->send_pkt_vec->len >= 1 && r->send_pkt_vec->p[r->send_pkt_vec->len - 1]->len == htons(12) && r->send_unack == r->send_pkt_vec->len
	&& r->recv_pkt_vec->len >= 1 && r->recv_lastack == r->recv_pkt_vec->len + 1 && r->recv_pkt_vec->p[r->recv_pkt_vec->len - 1]->len == htons(12)) {
		rel_destroy(r);
	}
}
void receive_ack(rel_t * r, uint32_t ackno) {
	/* handle ack events */
	if(ackno <= 1) {//check for proper ack
		return;
	}
	if(r->send_unsent < ackno - 1) {
		return;
	}
	if(r->send_pkt_vec->p[ackno - 2]->len == htons(12)) {// ack of EOF being received!
		r->send_unack = ackno - 1;
		try_destroy(r);
		return;
	}
	if(r->send_unack < ackno - 1) {//some packets being acked
		r->send_unack = ackno - 1;
		r->send_stamp = r->time;
	}else {                        //otherwise, do nothing.
		if(r->send_unack < r->send_unsent) {
			//send_pkt(r, r->send_pkt_vec->p[r->send_unack]);
		}
	}
	try_send(r);//when some packets being acked, there may be some packets ready to be sent.
}
void rel_recvpkt(rel_t * r, packet_t * pkt, size_t n) {
	/* receive a packet */
	uint16_t ck = pkt->cksum;//check for proper packet
	pkt->cksum = 0;
	if(ck != cksum(pkt, n)) {
		return;
	}
	if(ntohs(pkt->len) != n) {
		return;
	}
	
	//we must copy the packet because the memory which pkt is pointing to may be changed by rlib.c!
	packet_t * p1 = malloc(sizeof(packet_t));//ntohs(pkt->len));
	memcpy(p1, pkt, ntohs(pkt->len));
	pkt = p1;
	if(n < 8 || n > 512) {//test for proper length
		return;
	}
	//fprintf(stderr, "%d reccccc\n", pkt);
	if(n == 8) {//ack packet received
		uint32_t ackno = ntohl(pkt->ackno);
		receive_ack(r, ackno);
	}else if(n >= 12) {//real message 
		if(r->recv_pkt_vec->len >= 1 && r->recv_pkt_vec->p[r->recv_pkt_vec->len - 1]->len == htons(12) && ntohl(pkt->seqno) - 1 >= r->recv_pkt_vec->len) {//do not accept packets with seqno greater then the EOF packet
			return;
		}
		if(ntohl(pkt->seqno) - 1 < r->recv_output_pkt) {//a received packet has been received again. this may be caused by loss of ack packet. so we must send the ack again.
			try_ack(r, r->recv_lastack);
		}
		if(r->recv_output_pkt + r->window_size > ntohl(pkt->seqno) - 1) {//a new packet has been received
			while(r->recv_pkt_vec->len < ntohl(pkt->seqno)) {//reserve place for the new packet
				push_back_pkt(r->recv_pkt_vec, NULL);
			}
			if(r->recv_pkt_vec->p[ntohl(pkt->seqno) - 1] == NULL) {//do not replace existing packet
				r->recv_pkt_vec->p[ntohl(pkt->seqno) - 1] = pkt;//store the packet	
			}
		}
		
		receive_ack(r, ntohl(pkt->ackno));//deal with ack
		try_output(r);//try to output the new packet
	}
}
packet_t * new_packet(int seqno, char * tmp, int curlen) {
	/* produce a packet containing tmp[0..curlen - 1] */
	packet_t * pkt = malloc(sizeof(packet_t));
	pkt->len = htons(12 + curlen);
	pkt->seqno = htonl(seqno);
	memcpy(pkt->data, tmp, curlen);
	return pkt;
}
void rel_read(rel_t * s) {
	/* read inputs! */
	if(s->send_pkt_vec->len >= 1 && s->send_pkt_vec->p[s->send_pkt_vec->len - 1]->len == htons(12)) {//ignore inputs when EOF having been received
		return;
	}
	int curlen = 0;
	char tmp[500];
	int flag = 0;
	for(;;) {
		int x = conn_input(s->c, tmp + curlen, 1);//get one char
		if(x == 0) {//no char input
			break;
		}else if(x == -1) {//EOF input
			//tmp[curlen - 1] = EOF;
			flag = 1;
			break;
		}
		curlen++;
		if(curlen == 500) {//produce a packet if max packet length is archieved
			push_back_pkt(s->send_pkt_vec, NULL);
			s->send_pkt_vec->p[s->send_pkt_vec->len - 1] = new_packet(s->send_pkt_vec->len, tmp, curlen); 
			curlen = 0;
		}
	}
	if(curlen > 0) {//produce a packet for the remaining chars
		push_back_pkt(s->send_pkt_vec, NULL);
		s->send_pkt_vec->p[s->send_pkt_vec->len - 1] = new_packet(s->send_pkt_vec->len, tmp, curlen);
	}
	if(flag == 1) {//produce a EOF packet
		push_back_pkt(s->send_pkt_vec, NULL);
		s->send_pkt_vec->p[s->send_pkt_vec->len - 1] = new_packet(s->send_pkt_vec->len, tmp, 0);
	}
	try_send(s);
}

void try_ack (rel_t * r, int ackno) {
	/* send an ack packet */
	packet_t * p = malloc(sizeof(packet_t));
	p->len = 8;
	r->recv_lastack = ackno;
	p->ackno = htonl(ackno);
	p->len = htons(p->len);
	p->cksum = 0;
	p->cksum = cksum(p, 8);	
	conn_sendpkt(r->c, p, 8);
}

void try_output(rel_t * r) {
	/* try to output! */
	for( ; r->recv_pkt_vec->len > r->recv_output_pkt && r->recv_pkt_vec->p[r->recv_output_pkt] != NULL; ) {//whenever an unoutput packet exists
		if(r->recv_pkt_vec->p[r->recv_output_pkt]->len == htons(12)) {//a EOF packet
			conn_output(r->c, 0, 0);//output EOF
			try_ack(r, r->recv_output_pkt + 2);//the last ack
			r->recv_output_pkt++;
			try_destroy(r);//check whether the connection can be destroyed
			return;
		}
		int bufspace = conn_bufspace(r->c);//check for buffer space
		if(bufspace >= ntohs(r->recv_pkt_vec->p[r->recv_output_pkt]->len) - 12) {//if buffer space is enough for the whole packet
			conn_output(r->c, r->recv_pkt_vec->p[r->recv_output_pkt]->data, ntohs(r->recv_pkt_vec->p[r->recv_output_pkt]->len) - 12);
			r->recv_output_pkt++;//the next packet
			try_ack(r, r->recv_output_pkt + 1);//ack that packet
			continue;
		}else {
			break;
		}
	}
}
void rel_output(rel_t * r) {
	/* just try to output! */
	try_output(r);
}
void rel_timer () {
	/* Retransmit any packets that need to be retransmitted */
	rel_t * p = rel_list;
	while(p != NULL) {//walk though all the connections
		p->time++;
		if(p->send_unack < p->send_unsent && p->time > p->time_out + p->send_stamp) {//resend unacked packet
			send_pkt(p, p->send_pkt_vec->p[p->send_unack]);
		}
		p = p->next;
	}
}
