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
    private CCNHandle rccnHandle;
    private CCNHandle wccnHandle;
    private ContentName _prefix;

    public CCNIOImpl(CCNHandle ccnHandle) throws NullPointerException{
        try {
            this._prefix = ContentName.fromURI("ccnx:/");
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        }
        if(ccnHandle != null){
            this.wccnHandle = ccnHandle;
            try {
                this.rccnHandle = CCNHandle.open();
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
            CCNFileInputStream cis = new CCNFileInputStream(cn, rccnHandle);
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
            CCNFileOutputStream cos = new CCNFileOutputStream(cn, wccnHandle);
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
            CCNTime modificationTime = new CCNTime(new Date(System.currentTimeMillis()));
            ContentName versionedName = VersioningProfile.addVersion(
                    new ContentName(_prefix, interest.name().postfix(_prefix).components()), modificationTime);
            CCNFileOutputStream cfo = null;
            try {
                cfo = new CCNFileOutputStream(versionedName, wccnHandle);
                cfo.addOutstandingInterest(interest);
                return cfo;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public RepositoryFileOutputStream putRepoFile(String contentname) {
        try {
            ContentName cn = ContentName.fromURI(contentname);
            RepositoryFileOutputStream ros = new RepositoryFileOutputStream(cn, wccnHandle);
            return ros;
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
