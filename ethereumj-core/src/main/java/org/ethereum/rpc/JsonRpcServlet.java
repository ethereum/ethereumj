package org.ethereum.rpc;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import org.ethereum.facade.Ethereum;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JsonRpcServlet extends HttpServlet {

    public static Ethereum eth;
    private Web3Impl service;
    private JsonRpcServer jsonRpcServer;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException{
        jsonRpcServer.handle(req, resp);
    }

    protected void doGet (HttpServletRequest req, HttpServletResponse resp)
            throws IOException{
        jsonRpcServer.handle(req, resp);
    }

    public void init(ServletConfig config) {
        this.service = new Web3Impl(JsonRpcServlet.eth);
        this.jsonRpcServer = new JsonRpcServer(this.service, Web3Impl.class);
    }

}
