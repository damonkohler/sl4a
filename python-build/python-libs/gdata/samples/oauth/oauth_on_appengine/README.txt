NOTE:
main_hmac.py and main_rsa.py are the same sample.  The difference is the signature method used.
Both samples require that you register your web application to obtain a consumer key/secret.


To run main_hmac.py:

 - Register your web application as described here:
   http://code.google.com/apis/accounts/docs/RegistrationForWebAppsAuto.html

 - In main_hmac.py, change SETTINGs['CONSUMER_KEY'] and SETTINGs['CONSUMER_SECRET']
   to your own values provided in https://www.google.com/accounts/ManageDomains


To run main_rsa.py:

 - change 'main_hmac.py' to 'main_rsa.py' in app.yaml

 - Generate a self signing private key/certificate pair as described here:
   http://code.google.com/apis/gdata/oauth.html#GeneratingKeyCert

 - Register your web application as described here:
   http://code.google.com/apis/accounts/docs/RegistrationForWebAppsAuto.html
   
   Use https://www.google.com/accounts/ManageDomains to upload your certicate file.

 - In main_rsa.py, change the f = open('/path/to/your/rsa_private_key.pem') line
   to point to your .pem generated from the previous step.  Make sure the file is
   readable by your webserver.

   Also change SETTINGs['CONSUMER_KEY'] to your own value found in 
   https://www.google.com/accounts/ManageDomains. This value is typically your domain.
