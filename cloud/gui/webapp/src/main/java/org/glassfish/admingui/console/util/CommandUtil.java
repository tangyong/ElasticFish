/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admingui.console.util;

import java.util.*;

import org.glassfish.admingui.console.rest.RestUtil;

/**
 *
 * @author anilam
 */
public class CommandUtil {
    public static final String SERVICE_TYPE_RDMBS = "Database";
    public static final String SERVICE_TYPE_JAVAEE = "JavaEE";
    public static final String SERVICE_TYPE_LB = "LB";

    static final String REST_URL="http://localhost:4848/management/domain";

    /**
     *	<p> This method returns the list of Services. </p>
     *
     *	@param	appName	    Name of Application. This is optional parameter, can be set to NULL.
     *	@param	type        Service type. Possible value is "Cluster", "ClusterInstance", "database", "load_balancer", "JavaEE"
     *                      This is optional parameter, can be set to Null.
     *  @param  scope       Scope of services.  Possible value is "external", "shared", "application".
     *                      This is optional parameter, can be set to NULL.
     *
     *	@return	<code>List<Map></code>  Each map represents one Service.
     */
    public static List<Map> listServices(String appName, String type, String scope){

        List<Map>services = null;
        //String endpoint = GuiUtil.getSessionValue("REST_URL")+"/list-services";
        String endpoint = REST_URL+"/list-services";

        Map attrs = new HashMap();
        putOptionalAttrs(attrs, "appname", appName);
        putOptionalAttrs(attrs, "type", type);
        putOptionalAttrs(attrs, "scope", scope);


        services = RestUtil.getListFromREST(endpoint, attrs, "list");
        if (services == null){
            services = new ArrayList();
        }else {
            for(Map oneS : services){
                oneS.put("serviceName" , oneS.get("SERVICE-NAME"));
                oneS.put("serverType", oneS.get("SERVER-TYPE"));
                if ("Running".equals(oneS.get("STATE"))){
                    oneS.put("stateImage", "/images/running_small.gif");
                }
                else                
                    oneS.put("stateImage", "/images/not-running_small.png");
        }
        }
        //System.out.println("======== CommandUtil.listServices():  services = " + services);
        return services;
    }


    /**
     *	<p> This method returns the list of Names of the existing Templates. </p>
     *
     *	@param	serviceType Acceptable value is "JavaEE", "Database" "LoadBalancer".
     *                      If set to NULL, all service type will be returned.
     *	@return	<code>List<String></code>  Returns the list of names of the template.
     */
    public static List<String> getTemplateList(String type){

    //For now, since backend only supports one Virtualization setup, we will just return the list if anyone exist.
    //Later, probably need to pass in the virtualiztion type to this method.
        List<String> tList = new ArrayList();
        try{
            List<String> virts = RestUtil.getChildNameList(REST_URL+"/virtualizations");
            for(String virtType : virts){
                List<String> virtInstances = RestUtil.getChildNameList(REST_URL+"/virtualizations/" + virtType);
                if ( (virtInstances != null ) && (virtInstances.size() > 0)){
                    //get the templates for this V that is the same service type
                    String templateEndpoint = REST_URL+"/virtualizations/" + virtType + "/" + virtInstances.get(0) + "/template";
                    if (RestUtil.doesProxyExist(templateEndpoint )){
                        Map<String, String> templateEndpoints = RestUtil.getChildMap(templateEndpoint);

                        for(Map.Entry<String,String> endpointE : templateEndpoints.entrySet()){
                            Map<String, String> tempIndexes = RestUtil.getChildMap(endpointE.getValue() + "/template-index");
                            for(Map.Entry<String,String> indexE : tempIndexes.entrySet()){
                                Map attrs = RestUtil.getAttributesMap(indexE.getValue());
                                if ("ServiceType".equals (attrs.get("type"))  &&  type.equals(attrs.get("value"))){
                                    //finally found it
                                    tList.add(endpointE.getKey());
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception ex){

        }
        return tList;
    }

    /**
     *	<p> This method returns the list of of Services that is pre-selected by Orchestrator.  It is indexed by the serviceType.
     *  If a particular serviceType doesn't exist, it means the application doesn't require such service.  Service Configuration
     *  page in deployment wizard will not show that section.
     *
     *	@param	filePath  This is the absolute file path of the uploaded application.
     * 
     *	@return	raw meta data as returned by web service, e.g.
     *	<pre>
     *	{
     *    characteristics = {service-type = LB},
     *    init-type = lazy,
     *    name = basic_db_paas_sample-lb,
     *    service-type = LB,
     *  },
     *  {
     *    characteristics = {
     *      os-name = Linux,
     *      service-type = JavaEE,
     *    },
     *    configurations = {
     *      max.clustersize = 4,
     *      min.clustersize = 2
     *    },
     *    init-type = lazy,
     *    name = basic-db,
     *    service-type = JavaEE,
     *  },
     *  {
     *    characteristics = {
     *      os-name = 	Windows XP,
     *      service-type = Database,
     *    },
     *    init-type = lazy,
     *    name = default-derby-db-service,
     *    service-type = Database,
     *  }
     *	</pre>
    */
    public static List<Map<String, Object>> getPreSelectedServices(String filePath) {
        Map attrs = new HashMap();
        attrs.put("archive", filePath);
        try{
            Map appData = (Map) RestUtil.restRequest(REST_URL + "/applications/_get-service-metadata", attrs, "GET", null, null, false, true).get("data");
            List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map) appData.get("extraProperties")).get("list");
            System.out.println("endpoint="+REST_URL + "/applications/_get-service-metadata");
            System.out.println("payload="+attrs);
            System.out.println("========== _get-service-metadata returns: \n" + list);
            return list;
        }catch(Exception ex){
            System.out.println("Exception occurs:");
            System.out.println(REST_URL + "/applications/_get-service-metadata");
            System.out.println("attrs = " + attrs);
            return new ArrayList();
        }
        
    }

    private static void putOptionalAttrs(Map attrs, String key, String value){
        if (!GuiUtil.isEmpty(value)){
            attrs.put(key, value);
        }
    }

}
