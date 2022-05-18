#!/bin/bash

if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

curl 192.168.0.122/hivedrive.tar.gz --output hivedrive.tar.gz
tar xvzf hivedrive.tar.gz
sh hd_install/install.sh