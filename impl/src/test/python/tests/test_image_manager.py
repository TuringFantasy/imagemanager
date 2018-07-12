import os
from APIGateway import *

IMAGE_MANAGER_SERVICE="service/io.zolontech.services/image-manager/1.0.0/"
class TestUM:
    def setup(self):
	print("Initiating API Gateway Module")
	self.apigateway = APIGateway(host=os.getenv('MACAW_PLATFORM_URL'),
					access_key="5979ce4d-2700-4170-bc0b-608b665f73c0",
					secret_key="f729e369-7dea-47cb-9cd8-590dd3d51f20",
					login_method="access-key")
	self.apigateway.login()
 
    def teardown(self):
	self.apigateway.logout()
 
    def test_number_1(self):
        payload = {
                    "params" : [
                    {"address": "123 Washington Ave, Pleasanton, CA 94588",
                    "dob": 490037612000,
                    "name": "Cfx xyz Lee",
                    "emailId": "cfxxyzlee@dfgyue.com",
                    "userCode": "123450987"
                   }
                ]
        }

	response = self.apigateway.Post(IMAGE_MANAGER_SERVICE+"addUser", payload=payload)
	assert (response is not None), "Response is not received"
	assert (response.json() == "123450987"), "Not expected user code is received"
