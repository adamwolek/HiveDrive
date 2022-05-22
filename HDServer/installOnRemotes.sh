nodes=("192.168.0.120" "192.168.0.122" "192.168.0.70")
for i in "${nodes[@]}"
do
	ssh cc@$i "mkdir /home/cc/hivedrive"
	scp downloadAndInstall.sh cc@$i:/home/cc/hivedrive/
	ssh cc@$i "chmod +x /home/cc/hivedrive/downloadAndInstall.sh"
	ssh cc@$i "sudo ./home/cc/hivedrive/downloadAndInstall.sh"
done
