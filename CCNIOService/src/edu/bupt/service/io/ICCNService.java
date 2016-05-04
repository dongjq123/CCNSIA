package edu.bupt.service.io;

import org.ccnx.ccn.protocol.Interest;

import java.io.IOException;

/**
 * Created by fish on 16-5-4.
 */
public interface ICCNService {
    void execute(String args[], Interest interest) throws IOException;
}
