#!/bin/bash

function usage {
	echo ""
	echo "launcher.sh - tool for testing and demoing wireless networking in MobileXT"
	echo "	Please ensure related files guest.conf and domU.img reside in pwd"
	echo ""
	echo "To deploy, edit inside this script WPA_SUPP_INSERT to reflect your wifi"
	echo "  information and GUEST_IP to some appropriate static ip"
	echo ""
	echo "1. run ./launcher prep"
	echo "2. launch guest (shortcut ./launcher create)"
	echo "3. start console on guest (name is guest)"
	echo "4. on guest, modify ~/run.sh to match static ip and execute"
	echo ""
	echo "usage: ./launcher.sh COMMAND"
	echo "COMMAND = [prep|create|destroy]"
	echo ""
	echo "prep - once-per-boot preparations for launching guest with networking"
	echo "create - create guest with networking"
	echo "destroy - destroy guest and clean (prep remains in place)"
	echo ""
}

if [ -z $1 ]; then
	usage
	exit
fi

WPA_SUPP_INSERT='
network={
	ssid="WiFiRSU_6cd5c"
	#psk="8846cd5c"
	psk=af98bd00e50d594e204943e6e772a83832287bb3a0d42250e9b0a2c823ac6381
}
'

# For the purposes of this demonstration so far, we use a static IP address
# use the same subnet as the upstream router, with an address outside dhcp range
GUEST_IP="192.168.15.55"


if [[ $1 = "prep" || $1 = "prepare" || $1 = "p" ]]; then

	# mount guest image to loop0
	losetup /dev/loop0 ./domU.img

	# enable ip forwarding
	echo 1 >> /proc/sys/net/ipv4/ip_forward

	# insert connection information to wpa_supplicant.conf and connect
	killall wpa_supplicant
	echo "$WPA_SUPP_INSERT" >> /etc/wpa_supplicant.conf
	nohup wpa_supplicant -i wlan0 -c /etc/wpa_supplicant.conf &
	sleep 2

	# get dhcp address
	udhcpc -i wlan0

	# create bridge for guest virtual adapter
	brctl addbr xenbr0
	ip link set xenbr0 up
	# ip link set xenbr0 address $XENBR0_MAC

	# turn on proxy arp
	echo 1 >> /proc/sys/net/ipv4/conf/all/proxy_arp

	# add route to guest
	ip route add $GUEST_IP dev xenbr0
	
	echo ""
	echo "domU ready to launch"

elif [[ $1 = "create" || $1 = "c" ]]; then

	xl create ./guest.conf
	sleep 1

	echo "guest domU created"

elif [[ $1 = "destroy" || $1 = "d" ]]; then

	xl destroy guest
	echo "guest domU destroyed"

else
	usage

fi
