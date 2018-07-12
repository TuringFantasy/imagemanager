import requests
import logging
import json
import time
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.poolmanager import PoolManager
import ssl
import sys

class SSLAdapter(HTTPAdapter):
    '''An HTTPS Transport Adapter that uses an arbitrary SSL version.'''
    def __init__(self, ssl_version=None, **kwargs):
        self.ssl_version = ssl_version
        super(SSLAdapter, self).__init__(**kwargs)
    def init_poolmanager(self, connections, maxsize, block=False):
        self.poolmanager = PoolManager(num_pools=connections,
                                       maxsize=maxsize,
                                       block=block,
                                       ssl_version=self.ssl_version)

class APIGateway(object):    
    def __init__(self, **kwargs):
        self.host           = kwargs.get('host')
        self.port           = kwargs.get('port',443)
        self.protocol       = kwargs.get('protocol','https')
        self.username       = kwargs.get('username',None)
        self.password       = kwargs.get('password',None)
        self.access_key     = kwargs.get('access_key',None)
        self.secret_key     = kwargs.get('secret_key',None)
        self.loginmethod    = kwargs.get('login_method',None)
        self.token          = None
        self.loggedinTime   = int(round(time.time()))
        if self.host.find("http") == -1:
            self.apiRestBaseUrl = "%s://%s:%d/%s" % (self.protocol, self.host, self.port, "api_gateway")
        else:
            self.apiRestBaseUrl = "%s:%d/%s" % (self.host, self.port, "api_gateway")
        self.verify = kwargs.get('verify',False)
        self.debug_enabled  = False
        if not self.verify:
            from requests.packages.urllib3.exceptions import InsecureRequestWarning, SubjectAltNameWarning
            requests.packages.urllib3.disable_warnings(SubjectAltNameWarning)
            requests.packages.urllib3.disable_warnings(InsecureRequestWarning)   
        self.session = requests.Session()
        self.session.mount('https://', 
                            SSLAdapter(ssl_version=ssl.PROTOCOL_TLSv1_2))
        logging.info("Initialized Macaw APIGateway with REST End Point: %s" % (self.apiRestBaseUrl))

    def login(self,**kwargs):
        if self.token is not None:
            #Session is already logged in.
            return True
        if kwargs.get('login-method',None) or self.loginmethod:
            logging.info("Access Key based login requested")
            logininfo = {"access-key-id": kwargs.get('access_key') if kwargs.get('access_key',None) else self.access_key,
                            "secret-key": kwargs.get('secret_key') if kwargs.get('secret_key',None) else self.secret_key,
                            "login-method": kwargs.get('login_method') if kwargs.get('login_method',None) else self.loginmethod}
        else:
            logging.info("Username based login requested")
            logininfo = {"user": kwargs.get('username') if kwargs.get('username',None) else self.username,
                            "password": kwargs.get('password') if kwargs.get('password',None) else self.password}
        authresponse = None
        authresponse = self.Post("login",payload=logininfo)
        if authresponse is None:
            raise Exception("Login to APIGateway Failed.")
        self.loggedinTime   = int(round(time.time()))
        self.token          = authresponse.json()['apiGatewaySessionId']
        #logging.info("Authentication Token: %s"  % (str(self.token)))
        return True

    def logout(self):
        if self.debug_enabled:
            logging.info("Logging out of the API gateway and resetting the context.")
        response = None
        response = self.Post("logout",
                                payload=None)
        if response is None:
            raise Exception("Logout to APIGateway Failed.")
        self.token = None
        return True
  
    def renew(self):
        if self.debug_enabled:
            logging.info("Renewing APIGateway Token....")
        response = None
        response = self.Get("alive")
        if response is None:
            raise Exception("Renew Token Failed.")
    
    def Post(self,endpoint, **kwargs):
        if endpoint != "alive":
            if int(round(time.time())) - self.loggedinTime > 20 * 60:
                if self.debug_enabled:
                    print("Time to Renew Token...")
                self.loggedinTime = int(round(time.time()))
                self.renew()
        if self.token is not None:
            headers = {'content-type': 'application/json', 'X-Auth-Token': self.token}
        else:
            headers = {'content-type': 'application/json'}

        endpoint = "%s/%s" % (self.apiRestBaseUrl.rstrip("/"), endpoint.strip("/"))
        response = None
        payload = kwargs.get('payload',None)
        try:
            s_time = time.time() * 1000
            response = self.session.post(endpoint, 
                                        data=json.dumps(payload), 
                                        headers=headers,timeout=180,
                                        verify=self.verify)
            e_time = time.time() * 1000
            logging.debug("Response Time: %d (ms)" % (e_time-s_time))
            if response.status_code != 200:
                logging.info("Post REST Code: %d, Response: %s" % (response.status_code,response.text))     
                return None
        except Exception as e:
            raise Exception(e)
        return response
    
    def Get(self,endpoint):
        endpoint = "%s/%s" % (self.apiRestBaseUrl.rstrip("/"), endpoint.strip("/"))
        connect_timeout = 10
        response = None
        try:
            if self.token is not None:
                headers = {'content-type': 'application/json', 'X-Auth-Token': self.token}
            else:
                headers = {'content-type': 'application/json'}
            s_time = time.time() * 1000
            response = self.session.get(endpoint,headers=headers,
                                        timeout=(connect_timeout, 10),
                                        verify=self.verify)
            e_time = time.time() * 1000
            logging.debug("Response Time: %d (ms)" % (e_time-s_time))
            if response.status_code != 200:
                logging.error("Post REST Code: %d" % (response.status_code))     
                return None
        except Exception as e:
            raise Exception(e)
        return response
