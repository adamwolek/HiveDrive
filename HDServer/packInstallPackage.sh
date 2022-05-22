gradle build -x test 
tar cvzf hivedrive.tar.gz hd_install/
scp hivedrive.tar.gz cc@192.168.0.122:/var/www/html