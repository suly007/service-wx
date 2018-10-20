nohup java -Dspring.profiles.active=prod -Dserver.port=8001 -jar target/service-wx.jar >/dev/null 2>&1 &
nohup java -Dspring.profiles.active=prod -Dserver.port=8002 -jar target/service-wx.jar >/dev/null 2>&1 &
nohup java -Dspring.profiles.active=prod -Dserver.port=8003 -jar target/service-wx.jar >/dev/null 2>&1 &
nohup java -Dspring.profiles.active=prod -Dserver.port=8004 -jar target/service-wx.jar >/dev/null 2>&1 &


