package de.dytanic.cloudnet.wrapper.provider.service;

import com.google.common.base.Preconditions;
import de.dytanic.cloudnet.common.concurrent.CompletedTask;
import de.dytanic.cloudnet.common.concurrent.ITask;
import de.dytanic.cloudnet.driver.api.DriverAPIRequestType;
import de.dytanic.cloudnet.driver.api.ServiceDriverAPIResponse;
import de.dytanic.cloudnet.driver.network.INetworkClient;
import de.dytanic.cloudnet.driver.provider.service.SpecificCloudServiceProvider;
import de.dytanic.cloudnet.driver.serialization.ProtocolBuffer;
import de.dytanic.cloudnet.driver.service.*;
import de.dytanic.cloudnet.wrapper.DriverAPIUser;
import de.dytanic.cloudnet.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WrapperSpecificCloudServiceProvider implements SpecificCloudServiceProvider, DriverAPIUser {

    private final Wrapper wrapper;
    private UUID uniqueId;
    private String name;
    private ServiceInfoSnapshot serviceInfoSnapshot;

    public WrapperSpecificCloudServiceProvider(Wrapper wrapper, UUID uniqueId) {
        this.wrapper = wrapper;
        this.uniqueId = uniqueId;
    }

    public WrapperSpecificCloudServiceProvider(Wrapper wrapper, String name) {
        this.wrapper = wrapper;
        this.name = name;
    }

    public WrapperSpecificCloudServiceProvider(Wrapper wrapper, ServiceInfoSnapshot serviceInfoSnapshot) {
        this.wrapper = wrapper;
        this.serviceInfoSnapshot = serviceInfoSnapshot;
    }

    @Nullable
    @Override
    public ServiceInfoSnapshot getServiceInfoSnapshot() {
        if (this.serviceInfoSnapshot != null) {
            return this.serviceInfoSnapshot;
        }
        return this.getServiceInfoSnapshotAsync().get(5, TimeUnit.SECONDS, null);
    }

    @Override
    public boolean isValid() {
        return this.isValidAsync().get(5, TimeUnit.SECONDS, false);
    }

    @Override
    public @Nullable ServiceInfoSnapshot forceUpdateServiceInfo() {
        return this.forceUpdateServiceInfoAsync().get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<ServiceInfoSnapshot> getServiceInfoSnapshotAsync() {
        if (this.serviceInfoSnapshot != null) {
            return CompletedTask.create(this.serviceInfoSnapshot);
        }
        if (this.uniqueId != null) {
            return this.wrapper.getCloudServiceProvider().getCloudServiceAsync(this.uniqueId);
        }
        if (this.name != null) {
            return this.wrapper.getCloudServiceProvider().getCloudServiceByNameAsync(this.name);
        }
        throw new IllegalArgumentException("Cannot get ServiceInfoSnapshot without uniqueId or name");
    }

    @Override
    public @NotNull ITask<Boolean> isValidAsync() {
        return null; // TODO
    }

    @Override
    public @NotNull ITask<ServiceInfoSnapshot> forceUpdateServiceInfoAsync() {
        return this.executeDriverAPIMethod(
                DriverAPIRequestType.FORCE_UPDATE_SERVICE,
                this::writeDefaults,
                packet -> this.readDefaults(packet.getBody()).readOptionalObject(ServiceInfoSnapshot.class)
        );
    }

    @Override
    public void addServiceTemplate(@NotNull ServiceTemplate serviceTemplate) {
        this.addServiceTemplateAsync(serviceTemplate).get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> addServiceTemplateAsync(@NotNull ServiceTemplate serviceTemplate) {
        Preconditions.checkNotNull(serviceTemplate);

        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.ADD_SERVICE_TEMPLATE_TO_CLOUD_SERVICE,
                buffer -> this.writeDefaults(buffer).writeObject(serviceTemplate),
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    public void addServiceRemoteInclusion(@NotNull ServiceRemoteInclusion serviceRemoteInclusion) {
        this.addServiceRemoteInclusionAsync(serviceRemoteInclusion).get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> addServiceRemoteInclusionAsync(@NotNull ServiceRemoteInclusion serviceRemoteInclusion) {
        Preconditions.checkNotNull(serviceRemoteInclusion);

        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.ADD_SERVICE_REMOTE_INCLUSION_TO_CLOUD_SERVICE,
                buffer -> this.writeDefaults(buffer).writeObject(serviceRemoteInclusion),
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    public void addServiceDeployment(@NotNull ServiceDeployment serviceDeployment) {
        this.addServiceDeploymentAsync(serviceDeployment).get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> addServiceDeploymentAsync(@NotNull ServiceDeployment serviceDeployment) {
        Preconditions.checkNotNull(serviceDeployment);

        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.ADD_SERVICE_DEPLOYMENT_TO_CLOUD_SERVICE,
                buffer -> this.writeDefaults(buffer).writeObject(serviceDeployment),
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    public Queue<String> getCachedLogMessages() {
        return this.getCachedLogMessagesAsync().get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Queue<String>> getCachedLogMessagesAsync() {
        return this.executeDriverAPIMethod(
                DriverAPIRequestType.GET_CACHED_LOG_MESSAGES_FROM_CLOUD_SERVICE,
                this::writeDefaults,
                packet -> new LinkedBlockingQueue<>(this.readDefaults(packet.getBody()).readStringCollection())
        );
    }

    @Override
    public void setCloudServiceLifeCycle(@NotNull ServiceLifeCycle lifeCycle) {
        this.setCloudServiceLifeCycleAsync(lifeCycle).get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> setCloudServiceLifeCycleAsync(@NotNull ServiceLifeCycle lifeCycle) {
        Preconditions.checkNotNull(lifeCycle);

        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.SET_CLOUD_SERVICE_LIFE_CYCLE,
                buffer -> this.writeDefaults(buffer).writeEnumConstant(lifeCycle),
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    public void restart() {
        this.restartAsync().get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> restartAsync() {
        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.RESTART_CLOUD_SERVICE,
                this::writeDefaults,
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    public void kill() {
        this.killAsync().get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> killAsync() {
        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.KILL_CLOUD_SERVICE,
                this::writeDefaults,
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    public void runCommand(@NotNull String command) {
        this.runCommandAsync(command).get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> runCommandAsync(@NotNull String command) {
        Preconditions.checkNotNull(command);

        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.RUN_COMMAND_ON_CLOUD_SERVICE,
                buffer -> writeDefaults(buffer).writeString(command),
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    public void includeWaitingServiceTemplates() {
        this.includeWaitingServiceTemplatesAsync().get(5, TimeUnit.SECONDS, null);
    }

    @Override
    public void includeWaitingServiceInclusions() {
        this.includeWaitingServiceInclusionsAsync().get(5, TimeUnit.SECONDS, null);
    }

    @Override
    public void deployResources(boolean removeDeployments) {
        this.deployResourcesAsync(removeDeployments).get(5, TimeUnit.SECONDS, null);
    }

    @Override
    @NotNull
    public ITask<Void> includeWaitingServiceTemplatesAsync() {
        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.INCLUDE_WAITING_TEMPLATES_ON_CLOUD_SERVICE,
                this::writeDefaults,
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    @NotNull
    public ITask<Void> includeWaitingServiceInclusionsAsync() {
        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.INCLUDE_WAITING_INCLUSIONS_ON_CLOUD_SERVICE,
                this::writeDefaults,
                packet -> this.readDefaults(packet.getBody())
        );
    }

    @Override
    @NotNull
    public ITask<Void> deployResourcesAsync(boolean removeDeployments) {
        return this.executeVoidDriverAPIMethod(
                DriverAPIRequestType.DEPLOY_RESOURCES_ON_CLOUD_SERVICE,
                buffer -> this.writeDefaults(buffer).writeBoolean(removeDeployments),
                packet -> this.readDefaults(packet.getBody())
        );
    }

    private ProtocolBuffer writeDefaults(ProtocolBuffer buffer) {
        return buffer.writeOptionalUUID(this.serviceInfoSnapshot != null ? this.serviceInfoSnapshot.getServiceId().getUniqueId() : this.uniqueId).writeOptionalString(this.name);
    }

    private ProtocolBuffer readDefaults(ProtocolBuffer buffer) {
        ServiceDriverAPIResponse response = buffer.readEnumConstant(ServiceDriverAPIResponse.class);
        if (response == ServiceDriverAPIResponse.SERVICE_NOT_FOUND) {
            throw new IllegalArgumentException("The service of this provider doesn't exist");
        }
        return buffer;
    }

    @Override
    public INetworkClient getNetworkClient() {
        return this.wrapper.getNetworkClient();
    }
}
