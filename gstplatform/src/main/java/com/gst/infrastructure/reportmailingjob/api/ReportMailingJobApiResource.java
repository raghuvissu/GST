/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gst.infrastructure.reportmailingjob.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.gst.commands.domain.CommandWrapper;
import com.gst.commands.service.CommandWrapperBuilder;
import com.gst.commands.service.PortfolioCommandSourceWritePlatformService;
import com.gst.infrastructure.core.api.ApiRequestParameterHelper;
import com.gst.infrastructure.core.data.CommandProcessingResult;
import com.gst.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.gst.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.gst.infrastructure.core.service.Page;
import com.gst.infrastructure.core.service.SearchParameters;
import com.gst.infrastructure.reportmailingjob.ReportMailingJobConstants;
import com.gst.infrastructure.reportmailingjob.data.ReportMailingJobData;
import com.gst.infrastructure.reportmailingjob.service.ReportMailingJobReadPlatformService;
import com.gst.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/" + ReportMailingJobConstants.REPORT_MAILING_JOB_RESOURCE_NAME)
@Component
@Scope("singleton")
public class ReportMailingJobApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ReportMailingJobData> reportMailingToApiJsonSerializer;
    private final ReportMailingJobReadPlatformService reportMailingJobReadPlatformService;
    
    @Autowired
    public ReportMailingJobApiResource(final PlatformSecurityContext platformSecurityContext, 
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, 
            final ApiRequestParameterHelper apiRequestParameterHelper, 
            final DefaultToApiJsonSerializer<ReportMailingJobData> reportMailingToApiJsonSerializer, 
            final ReportMailingJobReadPlatformService reportMailingJobReadPlatformService) {
        this.platformSecurityContext = platformSecurityContext;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.reportMailingToApiJsonSerializer = reportMailingToApiJsonSerializer;
        this.reportMailingJobReadPlatformService = reportMailingJobReadPlatformService;
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createReportMailingJob(final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().
                createReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME).
                withJson(apiRequestBodyAsJson).build();
        
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        
        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @PUT
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateReportMailingJob(@PathParam("entityId") final Long entityId, final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().
                updateReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME, entityId).
                withJson(apiRequestBodyAsJson).build();
        
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        
        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @DELETE
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteReportMailingJob(@PathParam("entityId") final Long entityId, final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().
                deleteReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME, entityId).
                withJson(apiRequestBodyAsJson).build();
        
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        
        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @GET
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveReportMailingJob(@PathParam("entityId") final Long entityId, @Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        ReportMailingJobData reportMailingJobData = this.reportMailingJobReadPlatformService.retrieveReportMailingJob(entityId);
        
        if (settings.isTemplate()) {
            final ReportMailingJobData ReportMailingJobDataOptions = this.reportMailingJobReadPlatformService.retrieveReportMailingJobEnumOptions();
            reportMailingJobData = ReportMailingJobData.newInstance(reportMailingJobData, ReportMailingJobDataOptions);
        }
        
        return this.reportMailingToApiJsonSerializer.serialize(settings, reportMailingJobData, ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }
    
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveReportMailingJobTemplate(@Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final ReportMailingJobData ReportMailingJobDataOptions = this.reportMailingJobReadPlatformService.retrieveReportMailingJobEnumOptions();
        
        return this.reportMailingToApiJsonSerializer.serialize(settings, ReportMailingJobDataOptions, ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllReportMailingJobs(@Context final UriInfo uriInfo, 
            @QueryParam("offset") final Integer offset,
            @QueryParam("limit") final Integer limit, @QueryParam("orderBy") final String orderBy,
            @QueryParam("sortOrder") final String sortOrder) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final SearchParameters searchParameters = SearchParameters.fromReportMailingJob(offset, limit, orderBy, sortOrder);
        final Page<ReportMailingJobData> reportMailingJobData = this.reportMailingJobReadPlatformService.retrieveAllReportMailingJobs(searchParameters);
        
        return this.reportMailingToApiJsonSerializer.serialize(settings, reportMailingJobData, ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }
}
