package edu.bupt.service.io.impl;

import edu.bupt.service.io.CCNIOManage;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNFileOutputStream;
import org.ccnx.ccn.io.RepositoryFileOutputStream;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.CCNTime;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import java.io.IOException;
import java.util.Date;

/**
 * Created by fish on 16-4-22.
 */
public class CCNIOImpl implements CCNIOManage {
    private CCNHandle ccnHandle;

    public CCNIOImpl(CCNHandle ccnHandle) throws NullPointerException{
        if(ccnHandle != null){
            //this.ccnHandle = ccnHandle;
            try {
                this.ccnHandle = CCNHandle.open();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            throw new NullPointerException("CCNHandle should not be null");
        }

    }
    @Override
    public CCNFileInputStream getCCNFile(String contentName) {
        try {

            ContentName cn = ContentName.fromURI(contentName);
            System.out.println("getCCNFile:"+contentName);
            CCNFileInputStream cis = new CCNFileInputStream(cn, ccnHandle);
            return cis;
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public CCNFileOutputStream putCCNFile(String contentname) {
        try {
            ContentName cn = ContentName.fromURI(contentname);
            CCNFileOutputStream cos = new CCNFileOutputStream(cn, ccnHandle);
            return cos;
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public CCNFileOutputStream writeCCNBack(Interest interest) {
        if (interest != null) {
            CCNTime modificationTime = new CCNTime(new Date());
            ContentName versionedName = VersioningProfile.updateVersion(interest.name(), modificationTime);
            CCNFileOutputStream cfo = null;
            try {
                cfo = new CCNFileOutputStream(versionedName, ccnHandle);
            } catch (IOException e) {
                e.printStackTrace();
            }
            cfo.addOutstandingInterest(interest);
            return cfo;
        }else{
            return null;
        }
    }

    @Override
    public RepositoryFileOutputStream putRepoFile(String contentname) {
        try {
            ContentName cn = ContentName.fromURI(contentname);
            RepositoryFileOutputStream ros = new RepositoryFileOutputStream(cn, ccnHandle);
            return ros;
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
