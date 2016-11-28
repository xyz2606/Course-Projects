#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>

pthread_mutex_t first = PTHREAD_MUTEX_INITIALIZER,
				second = PTHREAD_MUTEX_INITIALIZER;

void *func_a() {
	int count = 2;
	while (count--) {
		printf("a");
		fflush(stdout);
		pthread_mutex_lock(&first);
		sleep(1);
		pthread_mutex_lock(&second);

		pthread_mutex_unlock(&second);
		pthread_mutex_unlock(&first);

	}
	return 0;
}

void *func_b() {
	int count = 2;
	while (count--) {
		printf("b");
		fflush(stdout);
		pthread_mutex_lock(&second);
		sleep(1);
		pthread_mutex_lock(&first);

		pthread_mutex_unlock(&first);
		pthread_mutex_unlock(&second);
	}
	return 0;
}
//pthread_mutex_t strange = PTHREAD_MUTEX_INITIALIZER;
int main() {
	//pthread_mutex_lock(&strange);
	//pthread_mutex_lock(&strange);
	printf("start\n");
	pthread_t a, b;
	pthread_create(&a, NULL, func_a, NULL);
	pthread_create(&b, NULL, func_b, NULL);

	pthread_join (a, NULL);
	pthread_join (b, NULL);
	return 0;
}
