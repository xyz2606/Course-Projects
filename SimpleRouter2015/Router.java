package edu.wisc.cs.sdn.sr;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.wisc.cs.sdn.sr.vns.VNSComm;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.util.MACAddress;

/**
 * @author Aaron Gember-Jacobson
 */
public class Router 
{
	/** User under which the router is running */
	private String user;
	
	/** Hostname for the router */
	private String host;
	
	/** Template name for the router; null if no template */
	private String template;
	
	/** Topology ID for the router */
	private short topo;
	
	/** List of the router's interfaces; maps interface name's to interfaces */
	private Map<String,Iface> interfaces;
	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/** PCAP dump file for logging all packets sent/received by the router;
	 *  null if packets should not be logged */
	private DumpFile logfile;
	
	/** Virtual Network Simulator communication manager for the router */
	private VNSComm vnsComm;

    /** RIP subsystem */
    private RIP rip;
	private short totId;
	/**
	 * Creates a router for a specific topology, host, and user.
	 * @param topo topology ID for the router
	 * @param host hostname for the router
	 * @param user user under which the router is running
	 * @param template template name for the router; null if no template
	 */
	public Router(short topo, String host, String user, String template)
	{
		this.topo = topo;
		this.host = host;
		this.setUser(user);
		this.template = template;
		this.logfile = null;
		this.interfaces = new HashMap<String,Iface>();
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache(this);
		this.vnsComm = null;
        	this.rip = new RIP(this);
		this.totId = 0;
	}
	
	public void init()
	{ this.rip.init(); }
	
	/**
	 * @param logfile PCAP dump file for logging all packets sent/received by 
	 * 		  the router; null if packets should not be logged
	 */
	public void setLogFile(DumpFile logfile)
	{ this.logfile = logfile; }
	
	/**
	 * @return PCAP dump file for logging all packets sent/received by the
	 *         router; null if packets should not be logged
	 */
	public DumpFile getLogFile()
	{ return this.logfile; }
	
	/**
	 * @param template template name for the router; null if no template
	 */
	public void setTemplate(String template)
	{ this.template = template; }
	
	/**
	 * @return template template name for the router; null if no template
	 */
	public String getTemplate()
	{ return this.template; }
		
	/**
	 * @param user user under which the router is running; if null, use current 
	 *        system user
	 */
	public void setUser(String user)
	{
		if (null == user)
		{ this.user = System.getProperty("user.name"); }
		else
		{ this.user = user; }
	}
	
	/**
	 * @return user under which the router is running
	 */
	public String getUser()
	{ return this.user; }
	
	/**
	 * @return hostname for the router
	 */
	public String getHost()
	{ return this.host; }
	
	/**
	 * @return topology ID for the router
	 */
	public short getTopo()
	{ return this.topo; }
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * @return list of the router's interfaces; maps interface name's to
	 * 	       interfaces
	 */
	public Map<String,Iface> getInterfaces()
	{ return this.interfaces; }
	
	/**
	 * @param vnsComm Virtual Network System communication manager for the router
	 */
	public void setVNSComm(VNSComm vnsComm)
	{ this.vnsComm = vnsComm; }
	
	/**
	 * Close the PCAP dump file for the router, if logging is enabled.
	 */
	public void destroy()
	{
		if (logfile != null)
		{ this.logfile.close(); }
	}
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loading routing table");
		System.out.println("---------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("---------------------------------------------");
	}
	
	/**
	 * Add an interface to the router.
	 * @param ifaceName the name of the interface
	 */
	public Iface addInterface(String ifaceName)
	{
		Iface iface = new Iface(ifaceName);
		this.interfaces.put(ifaceName, iface);
		return iface;
	}

	/**
	 * Gets an interface on the router by the interface's name.
	 * @param ifaceName name of the desired interface
	 * @return requested interface; null if no interface with the given name 
	 * 		   exists
	 */
	public Iface getInterface(String ifaceName)
	{ return this.interfaces.get(ifaceName); }

	/**
	 * Send an Ethernet packet out a specific interface.
	 * @param etherPacket an Ethernet packet with all fields, encapsulated
	 * 		  headers, and payloads completed
	 * @param iface interface on which to send the packet
	 * @return true if the packet was sent successfully, otherwise false
	 */
	public boolean sendPacket(Ethernet etherPacket, Iface iface)
	{ return this.vnsComm.sendPacket(etherPacket, iface.getName()); }
	public RouteTableEntry getRTE(int ip) {
		for(int i = 32; i >= 0; i--) {
			int msk = i == 32 ? -1 : ((1 << i) - 1) << (32 - i);
			RouteTableEntry rte = this.routeTable.findEntry(ip & msk, msk);
			if(rte != null) {
				return rte;
			}
		}
		return null;
	}

	public void sendIp(IPv4 resIp, Iface outIface, int outIp) {
		resIp.setChecksum((short) 0);
		resIp.serialize();
		resIp.setParent(new Ethernet());
		Ethernet returnPacket = (Ethernet)resIp.getParent();
		returnPacket.setPayload(resIp);
		returnPacket.setPriorityCode((byte)0);
		returnPacket.setEtherType(Ethernet.TYPE_IPv4);
		returnPacket.setSourceMACAddress(outIface.getMacAddress().toBytes());
		ArpEntry ae = this.arpCache.lookup(outIp);
		if(ae == null) {
			this.arpCache.waitForArp(returnPacket, outIface, outIp);
		}else {
			returnPacket.setDestinationMACAddress(ae.getMac().toBytes());
			this.vnsComm.sendPacket(returnPacket, outIface.getName());
		}
	}

	public void sendICMP(int type, int code, int ip) {
		for (Iface iface : this.interfaces.values()) {
			if(iface.getIpAddress() == ip) {
				return;
			}
		}
		ICMP returnPacket = new ICMP();
		returnPacket.setIcmpType((byte)type);
		returnPacket.setIcmpCode((byte)code);
		returnPacket.setChecksum((short)0);
		returnPacket.serialize();

		RouteTableEntry rte = getRTE(ip);
		
		if(rte == null) {
			return;
		}
		Iface outIface = interfaces.get(rte.getInterface());
		int outIp = rte.getGatewayAddress();
		if(outIp == 0) {
			outIp = ip;
		}
		returnPacket.setParent(new IPv4());
		IPv4 resIp = (IPv4)returnPacket.getParent();
		resIp.setPayload(returnPacket);
		resIp.setDestinationAddress(ip);
		resIp.setSourceAddress(outIface.getIpAddress());
		resIp.setTtl((byte)16);
		resIp.setProtocol(IPv4.PROTOCOL_ICMP);
		resIp.setFragmentOffset((short)0);
		resIp.setFlags((byte)0);
		resIp.setOptions(new byte[0]);
		resIp.setIdentification(totId++);		
		resIp.setChecksum((short)0);
		resIp.serialize();
		sendIp(resIp, outIface, outIp);
	}
	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		if (etherPacket.getEtherType() != -31011)
			System.out.println("*** -> Received packet: " +
					etherPacket.toString().replace("\n", "\n\t"));

		/********************************************************************/
		/* TODO: Handle packets                                             */
		if (etherPacket.getEtherType() == Ethernet.TYPE_ARP) {
			handleArpPacket(etherPacket, inIface);
		}else if(etherPacket.getEtherType() == Ethernet.TYPE_IPv4) {
			IPv4 ipPacket = (IPv4)etherPacket.getPayload();
			int targetIp = ipPacket.getDestinationAddress();
			int isMyself = 0;
			for (Iface iface : this.interfaces.values()) {
				if(iface.getIpAddress() == targetIp || targetIp == Util.dottedDecimalToInt("224.0.0.9")) {
					isMyself = 1;
					break;
				}
			}
			if(isMyself == 0) {
				short checksum = ipPacket.getChecksum();
				ipPacket.setChecksum((short)0);
				ipPacket.serialize();
				if(checksum == ipPacket.getChecksum()) {
					ipPacket.setTtl((byte)(ipPacket.getTtl() - 1));
					if(ipPacket.getTtl() == 0) {
						sendICMP(11, 0, ipPacket.getSourceAddress());
						return;
					}

					RouteTableEntry rte = getRTE(targetIp);
					if(rte == null) {
						sendICMP(3, 0, ipPacket.getSourceAddress());

					}else {
						Iface outIface = interfaces.get(rte.getInterface());
						int outIp = rte.getGatewayAddress();
						if(outIp == 0) {
							outIp = ipPacket.getDestinationAddress();
						}
						sendIp(ipPacket, outIface, outIp);
					}
					
				} else {
					//ignore
				}
			} else {		
				if(ipPacket.getProtocol() == IPv4.PROTOCOL_ICMP) {
					ICMP icmpPacket = (ICMP)ipPacket.getPayload();
					short checksum = icmpPacket.getChecksum();
					icmpPacket.setChecksum((short)0);
					icmpPacket.serialize();
					if(checksum == icmpPacket.getChecksum()) {//compute chksum for all bits!
						if(icmpPacket.getIcmpType() == 8 && icmpPacket.getIcmpCode() == 0) {
							sendICMP(0, 0, ipPacket.getSourceAddress());
						}
					} else {
						//ignore
					}
				}else if(ipPacket.getProtocol() == IPv4.PROTOCOL_UDP) {					
					UDP udpPacket = (UDP)ipPacket.getPayload();
					short checksum = udpPacket.getChecksum();					
			    	udpPacket.setChecksum((short) 0);
					udpPacket.serialize();
					udpPacket.setParent(ipPacket);	

					if(checksum == udpPacket.getChecksum()) {					
						if(udpPacket.getDestinationPort() == 520) {
							this.rip.handlePacket(etherPacket, inIface);
						}else {
							sendICMP(3, 3, ipPacket.getSourceAddress());
						}
					} else {
						//ignore
					}
				}else if(ipPacket.getProtocol() == IPv4.PROTOCOL_TCP) {
					sendICMP(3, 0, ipPacket.getSourceAddress());
				} else {
					//ignore
				}
			}
		}else {
			System.out.println("ERROR: Router.java: raw etherPacket is not a IP or ARP packet!");
		}
		/********************************************************************/
	}

	/**
	 * Handle an ARP packet received on a specific interface.
	 * @param etherPacket the complete ARP packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	private void handleArpPacket(Ethernet etherPacket, Iface inIface)
	{
		// Make sure it's an ARP packet
		if (etherPacket.getEtherType() != Ethernet.TYPE_ARP)
		{ return; }

		// Get ARP header
		ARP arpPacket = (ARP)etherPacket.getPayload();
		int targetIp = ByteBuffer.wrap(
				arpPacket.getTargetProtocolAddress()).getInt();

		switch(arpPacket.getOpCode())
		{
			case ARP.OP_REQUEST:
				// Check if request is for one of my interfaces
				if (targetIp == inIface.getIpAddress())
				{ this.arpCache.sendArpReply(etherPacket, inIface); }
				break;
			case ARP.OP_REPLY:
				// Check if reply is for one of my interfaces
				if (targetIp != inIface.getIpAddress())
				{ break; }

				// Update ARP cache with contents of ARP reply
				int senderIp = ByteBuffer.wrap(
						arpPacket.getSenderProtocolAddress()).getInt();
				ArpRequest request = this.arpCache.insert(
						new MACAddress(arpPacket.getSenderHardwareAddress()),
						senderIp);

				// Process pending ARP request entry, if there is one
				if (request != null)
				{				
					for (Ethernet packet : request.getWaitingPackets())
					{
						/*********************************************************/
						/* TODO: send packet waiting on this request             */
						packet.setDestinationMACAddress(arpPacket.getSenderHardwareAddress());
						this.vnsComm.sendPacket(packet, request.getIface().getName());
						/*********************************************************/						
					}
				}
				break;
		}
	}
}
