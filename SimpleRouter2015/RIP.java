package edu.wisc.cs.sdn.sr;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.RIPv2;
import net.floodlightcontroller.packet.RIPv2Entry;
import net.floodlightcontroller.packet.UDP;

/**
  * Implements RIP. 
  * @author Anubhavnidhi Abhashkumar and Aaron Gember-Jacobson
  */
public class RIP implements Runnable
{
    private static final int RIP_MULTICAST_IP = 0xE0000009;
    private static final byte[] BROADCAST_MAC = {(byte)0xFF, (byte)0xFF, 
            (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
    
    /** Send RIP updates every 10 seconds */
    private static final int UPDATE_INTERVAL = 10;

    /** Timeout routes that neighbors last advertised more than 30 seconds ago*/
    private static final int TIMEOUT = 30;

    /** Router whose route table is being managed */
	private Router router;

    /** Thread for periodic tasks */
    private Thread tasksThread;
    
    private Map<Integer, Integer> RIPTable;
    private Map<Integer, Long> RIPTimer;

	public RIP(Router router)
	{ 
        this.router = router; 
        this.tasksThread = new Thread(this);
        this.RIPTable = new TreeMap<>();
        this.RIPTimer = new TreeMap<>();
    }

	public void init()
	{
        // If we are using static routing, then don't do anything
        if (this.router.getRouteTable().getEntries().size() > 0)
        { return; }

        System.out.println("RIP: Build initial routing table");
        for(Iface iface : this.router.getInterfaces().values())
        {
            this.router.getRouteTable().addEntry(
                    (iface.getIpAddress() & iface.getSubnetMask()),
                    0, // No gateway for subnets this router is connected to
                    iface.getSubnetMask(), iface.getName());
        }
        System.out.println("Route Table:\n"+this.router.getRouteTable());

		this.tasksThread.start();

        /*********************************************************************/
        /* TODO: Add other initialization code as necessary                  */
		for(Iface iface: this.router.getInterfaces().values()) {
			RIPv2 rip = new RIPv2();
			rip.setCommand(RIPv2.COMMAND_REQUEST);
			List<RIPv2Entry> sendList = new ArrayList<>();
			for(RouteTableEntry it:router.getRouteTable().getEntries()) {
				RIPv2Entry temp = new RIPv2Entry(it.getDestinationAddress(), it.getMaskAddress(), 1);
                RIPTable.put(temp.getAddress(), temp.getMetric());
                RIPTimer.put(temp.getAddress(), System.currentTimeMillis());
				temp.setNextHopAddress(iface.getIpAddress());
				sendList.add(temp);                
			}
			rip.setEntries(sendList);
			sendRIPPakcet(rip,iface,RIP_MULTICAST_IP,BROADCAST_MAC);
		}
        /*********************************************************************/
	}

    private boolean sendRIPPakcet(RIPv2 rip, Iface iface, int destinationIP, byte[] destinationMAC) {
    	UDP udp = new UDP();
    	udp.setDestinationPort(UDP.RIP_PORT);
    	udp.setSourcePort(UDP.RIP_PORT);
    	udp.setPayload(rip);
    	udp.setChecksum((short) 0);
		udp.serialize();    	   
    	IPv4 ipv4 = new IPv4();
    	ipv4.setDestinationAddress(destinationIP);
    	ipv4.setSourceAddress(iface.getIpAddress());
    	ipv4.setProtocol(IPv4.PROTOCOL_UDP);
    	ipv4.setPayload(udp);
    	ipv4.setTtl((byte) TIMEOUT);
    	ipv4.setChecksum((short) 0);
    	ipv4.serialize();
        ((UDP)ipv4.getPayload()).setChecksum((short)0);
        ((UDP)ipv4.getPayload()).serialize();   
		Ethernet ethernet = new Ethernet();
		ethernet.setDestinationMACAddress(destinationMAC);
		ethernet.setPayload(ipv4);
		ethernet.setSourceMACAddress(iface.getMacAddress().toBytes());
		ethernet.setEtherType(Ethernet.TYPE_IPv4);
		return this.router.sendPacket(ethernet, iface);
	}

	/**
      * Handle a RIP packet received by the router.
      * @param etherPacket the Ethernet packet that was received
      * @param inIface the interface on which the packet was received
      */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
        // Make sure it is in fact a RIP packet
        if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4)
        { return; } 
		IPv4 ipPacket = (IPv4)etherPacket.getPayload();
        if (ipPacket.getProtocol() != IPv4.PROTOCOL_UDP)
        { return; } 
		UDP udpPacket = (UDP)ipPacket.getPayload();
        if (udpPacket.getDestinationPort() != UDP.RIP_PORT)
        { return; }
		RIPv2 ripPacket = (RIPv2)udpPacket.getPayload();

        /*********************************************************************/
        /* TODO: Handle RIP packet                                           */
        System.out.println("received RIP packet");
		for (RIPv2Entry ripEntry:ripPacket.getEntries()) {            
			if(ripEntry.getMetric() >= 16) continue;
			RouteTableEntry routerTableEntry = router.getRouteTable().findEntry(ripEntry.getAddress(), ripEntry.getSubnetMask());
			if (routerTableEntry == null) {
				router.getRouteTable().addEntry(ripEntry.getAddress(), ripEntry.getNextHopAddress(), ripEntry.getSubnetMask(), inIface.getName());
				RIPTable.put(ripEntry.getAddress(), ripEntry.getMetric()+1);
				RIPTimer.put(ripEntry.getAddress(), System.currentTimeMillis());
			} else {
				int oldCost = RIPTable.get(routerTableEntry.getDestinationAddress());
				int newCost = ripEntry.getMetric() + 1;
				if(newCost < oldCost) {
					router.getRouteTable().updateEntry(ripEntry.getAddress(), ripEntry.getNextHopAddress(), ipPacket.getSourceAddress(), inIface.getName());
					RIPTable.put(ripEntry.getAddress(), newCost);
					RIPTimer.put(ripEntry.getAddress(), System.currentTimeMillis());
				}
			}
		}
		
		if (ripPacket.getCommand()==RIPv2.COMMAND_REQUEST) {
			RIPv2 ripRes = new RIPv2();
			ripRes.setCommand(RIPv2.COMMAND_RESPONSE);
			List<RIPv2Entry> res = new ArrayList<RIPv2Entry>();
			for(RouteTableEntry e:router.getRouteTable().getEntries()){			
				if(e.getInterface().equals(inIface.getName())) {
					RIPv2Entry entry = new RIPv2Entry(e.getDestinationAddress(), e.getMaskAddress(), RIPTable.get(e.getDestinationAddress()));
					entry.setNextHopAddress(inIface.getIpAddress());
					res.add(entry);
				}
			}
			ripRes.setEntries(res);
			sendRIPPakcet(ripRes, inIface, ipPacket.getSourceAddress(), etherPacket.getSourceMACAddress());
		}

        System.out.println("Route Table:\n"+this.router.getRouteTable());
        /*********************************************************************/
	}
    
    /**
      * Perform periodic RIP tasks.
      */
	@Override
	public void run() 
    {
        /*********************************************************************/
        /* TODO: Send period updates and time out route table entries        */
		try {
			Thread.sleep(UPDATE_INTERVAL * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ListIterator<RouteTableEntry> it = router.getRouteTable().getEntries().listIterator();
		while(it.hasNext()) {
			RouteTableEntry e = it.next();
			if(e.getGatewayAddress() == 0) continue;
			if(System.currentTimeMillis()-RIPTimer.get(e.getDestinationAddress()) >= RIP.TIMEOUT * 1000) {
				synchronized (router.getRouteTable().getEntries()) {
					it.remove();
				}
			}
		}        
		for(Iface iface:router.getInterfaces().values()) {
			RIPv2 ripRes = new RIPv2();
			ripRes.setCommand(RIPv2.COMMAND_RESPONSE);
			ListIterator<RouteTableEntry> it1 = router.getRouteTable().getEntries().listIterator();
			List<RIPv2Entry> res = new ArrayList<RIPv2Entry>();
			while(it1.hasNext()) {
				RouteTableEntry e = it1.next();				
				RIPv2Entry entry = new RIPv2Entry(e.getDestinationAddress(), e.getMaskAddress(), RIPTable.get(e.getDestinationAddress()));
				entry.setNextHopAddress(iface.getIpAddress());
				res.add(entry);
			}
			ripRes.setEntries(res);
			sendRIPPakcet(ripRes, iface, RIP_MULTICAST_IP, BROADCAST_MAC);            
		}
        /*********************************************************************/
	}
}
