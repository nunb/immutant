/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.immutant.daemons;

import org.immutant.daemons.as.DaemonServices;
import org.jboss.as.jmx.MBeanRegistrationService;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.ImmediateValue;
import org.projectodd.polyglot.core_extensions.AtRuntimeInstaller;


public class Daemonizer extends AtRuntimeInstaller<Daemonizer> {

    public Daemonizer(DeploymentUnit unit) {
        super( unit );
    }

    public void deploy(final String daemonName, Runnable start, Runnable stop, boolean singleton) {

        Daemon daemon = new Daemon(start, stop);
        DaemonService daemonService = new DaemonService( daemon );
        ServiceName serviceName = DaemonServices.daemon( getUnit(), daemonName );
        deploy( serviceName, daemonService, singleton );

        installMBean( serviceName,
                new MBeanRegistrationService<DaemonMBean>( mbeanName( "immutant.daemons", serviceName ), 
                        new ImmediateValue<DaemonMBean>( daemon ) ) ); 

    }

}
