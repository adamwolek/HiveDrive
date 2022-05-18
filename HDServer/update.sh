#!/bin/bash

if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

curl 192.168.0.122/hivedrive.tar.gz --output hivedrive.tar.gz
tar xvzf hivedrive.tar.gz
cp hd_install/install_resources/HDServer.jar /usr/share/hivedrive
systemctl restart hivedrive.service