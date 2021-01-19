package com.offbytwo.jenkins.model.credentials;

import com.offbytwo.jenkins.client.JenkinsHttpConnection;
import com.offbytwo.jenkins.model.BaseModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobCredentialManager extends CredentialManager {

    public static final String V2URL_JOB = "/job/%s/credentials/store/folder/domain/_";

    private String jobName = "";

    public JobCredentialManager(String jobName, String version, JenkinsHttpConnection client) {
        super(version, client);
        this.jobName = jobName;
        this.baseUrl = V2URL_JOB;
    }

    @Override
    public Map<String, Credential> listCredentials() throws IOException {
        String url = String.format(this.baseUrl + "?depth=2", this.jobName);
        if (this.isVersion1) {
            CredentialResponseV1 response = this.jenkinsClient.get(url, CredentialResponseV1.class);
            Map<String, Credential> credentials = response.getCredentials();
            //need to set the id on the credentials as it is not returned in the body
            for (String crendentialId : credentials.keySet()) {
                credentials.get(crendentialId).setId(crendentialId);
            }
            return credentials;
        } else {
            CredentialResponse response = this.jenkinsClient.get(url, CredentialResponse.class);
            List<Credential> credentials = response.getCredentials();
            Map<String, Credential> credentialMap = new HashMap<>();
            for(Credential credential : credentials) {
                credentialMap.put(credential.getId(), credential);
            }
            return credentialMap;
        }
    }

    @Override
    public void createCredential(Credential credential, Boolean crumbFlag) throws IOException {
        String url = String.format(this.baseUrl + "/%s?", this.jobName, "createCredentials");
        if (credential.useMultipartForm()) {
            this.jenkinsClient.post_multipart_form_json(url, credential.dataForCreate(), crumbFlag);
        } else {
            this.jenkinsClient.post_form_json(url, credential.dataForCreate(), crumbFlag);
        }
    }

    @Override
    public void updateCredential(String credentialId, Credential credential, Boolean crumbFlag) throws IOException {
        credential.setId(credentialId);
        String url = String.format(this.baseUrl + "/%s/%s/%s?", this.jobName, "credential", credentialId, "updateSubmit");
        if (credential.useMultipartForm()) {
            this.jenkinsClient.post_multipart_form_json(url, credential.dataForUpdate(), crumbFlag);
        } else {
            this.jenkinsClient.post_form_json(url, credential.dataForUpdate(), crumbFlag);
        }
    }

    @Override
    public void deleteCredential(String credentialId, Boolean crumbFlag) throws IOException {
        String url = String.format(this.baseUrl + "/%s/%s/%s?", this.jobName, "credential", credentialId, "doDelete");
        this.jenkinsClient.post_form(url, new HashMap<String, String>(), crumbFlag);
    }

}
