package com.ligitalsoft.defcat.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.2.10
 * Wed Apr 20 15:07:11 CST 2011
 * Generated source version: 2.2.10
 * 
 */
 
@WebService(targetNamespace = "http://webservice.defcat.ligitalsoft.com/", name = "catalogWebService")
@XmlSeeAlso({ObjectFactory.class})
public interface CatalogWebService {

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "findCatalogs", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.FindCatalogs")
    @WebMethod
    @ResponseWrapper(localName = "findCatalogsResponse", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.FindCatalogsResponse")
    public java.lang.String findCatalogs();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "findCatalogTreeById", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.FindCatalogTreeById")
    @WebMethod
    @ResponseWrapper(localName = "findCatalogTreeByIdResponse", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.FindCatalogTreeByIdResponse")
    public java.lang.String findCatalogTreeById(
        @WebParam(name = "catalogId", targetNamespace = "")
        java.lang.Long catalogId
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "transmission", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.Transmission")
    @WebMethod
    @ResponseWrapper(localName = "transmissionResponse", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.TransmissionResponse")
    public java.lang.String transmission(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );
    
    /**
     * 指标项
     * @param arg0
     * @return
     * @author lifh
     */
    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "changeItemTran", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.ChangeItemTran")
    @WebMethod
    @ResponseWrapper(localName = "changeItemTranResponse", targetNamespace = "http://webservice.defcat.ligitalsoft.com/", className = "com.ligitalsoft.defcat.webservice.ChangeItemTranResponse")
    public java.lang.String changeItemTran(
                    java.lang.String xml,String categoryIds, Long catalogId
                );
}
