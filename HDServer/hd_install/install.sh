#!/bin/bash
#export HIVEDRIVE_HOME=$PWD
#echo $HIVEDRIVE_HOME

if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi


#Copying to installation folder
rm -rf /usr/share/hivedrive
mkdir /usr/share/hivedrive
cp -r install_resources/* /usr/share/hivedrive
chmod +x /usr/share/hivedrive/updateHiveDrive.sh

#Creating service
mv /usr/share/hivedrive/hivedrive.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable hivedrive.service
systemctl start hivedrive.service


