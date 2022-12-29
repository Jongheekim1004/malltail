# 

## Model
www.msaez.io/#/storming/9193c1c4a611b2f6d8832acb7d0af300

## Before Running Services
### Make sure there is a Kafka server running
```
cd kafka
docker-compose up
```
- Check the Kafka messages:
```
cd kafka
docker-compose exec -it kafka /bin/bash
cd /bin
./kafka-console-consumer --bootstrap-server localhost:9092 --topic
```

## Run the backend micro-services
See the README.md files inside the each microservices directory:

- order
- pay
- shop
- shipping
- delivery
- viewPage


## Run API Gateway (Spring Gateway)
```
cd gateway
mvn spring-boot:run
```

## Test by API
- order
```
 http :8088/orders id="id" CustomerId="CustomerId" itemNo="itemNo" qty="qty" createDate="createDate" orderStatus="orderStatus" deliveryStatus="deliveryStatus" customerAddress="customerAddress" customerName="customerName" phoneNumber="phoneNumber" updateDate="updateDate" 
```
- pay
```
 http :8088/payments id="id" payId="payId" orderNo="orderNo" paystatus="paystatus" itemNo="itemNo" 
```
- shop
```
 http :8088/shopManagements id="id" orderNo="orderNo" CustomerId="CustomerId" ItemNo="ItemNo" qty="qty" createDate="createDate" orderStatus="orderStatus" deliveryStatus="deliveryStatus" CustomerAddress="CustomerAddress" shippingAddress="shippingAddress" payId="payId" CustomerName="CustomerName" phoneNumber="phoneNumber" shopName="shopName" 
```
- shipping
```
 http :8088/shippings id="id" orderNo="orderNo" shippingAddress="shippingAddress" shippingStatus="shippingStatus" transportNo="transportNo" sender="sender" receiver="receiver" phoneNumber="phoneNumber" 
```
- delivery
```
 http :8088/deliveries id="id" orderNo="orderNo" deliveryAddress="deliveryAddress" customsStatus="customsStatus" deliveryStatus="deliveryStatus" InvoiceNo="InvoiceNo" sender="sender" receiver="receiver" phoneNumber="phoneNumber" 
```
- viewPage
```
```


## Run the frontend
```
cd frontend
npm i
npm run serve
```

## Test by UI
Open a browser to localhost:8088

## Required Utilities

- httpie (alternative for curl / POSTMAN) and network utils
```
sudo apt-get update
sudo apt-get install net-tools
sudo apt install iputils-ping
pip install httpie
```

- kubernetes utilities (kubectl)
```
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

- aws cli (aws)
```
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

- eksctl 
```
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

