# unimelb-mf-graphite-plugin
Graphite plugin for Mediaflux. It includes plugin services to send metrics to Graphite carbon server.


## 1. Installation
* In Mediaflux Aterm run following commands:
  * **`package.install :in https://github.com/UoM-ResPlat-DevOps/unimelb-mf-graphite-plugin/releases/download/v0.0.4/mfpkg-unimelb-mf-graphite-plugin-0.0.4.zip`**
  * **`srefresh`**

## 2. Send metrics to Graphite carbon server
* In Mediaflux Aterm,
  * **`graphite.metrics.send :host your.graphite-carbon-server.org :port 2004 :protocol pickle :server-metrics all`**
* You can also call other services to retrieve metric value, e.g.
  * **`graphite.metrics.send :host your.graphite-carbon-server.org :port 2004 :protocol pickle :metric < :path system.user.count :service -name authentication.user.count -xpath size < :domain system > >`**
* The usage of **graphite.metrics.send** see below:
```
> help graphite.metrics.send
help: graphite.metrics.send
	synopsis:
		Sends the metrics to Graphite server.

	arguments:
		:host (type=string, max-occurs=1) Graphite carbon server host.
		:metric (type=document, min-occurs=0) Metric.
			:path (type=string, max-occurs=1) Metric path.
			:service (type=document, min-occurs=0, max-occurs=1, ignore-descendants=true) The service to retrieve the metric value.
				-name (type=string) Service name.
				-xpath (type=string) XPATH to retrieve the value from service result.
			:time (type=date, min-occurs=0, max-occurs=1) Metric timestamp. Defaults to current server time.
			:value (type=string, min-occurs=0, max-occurs=1) Metric value.
		:port (type=integer, min-occurs=0, max-occurs=1) Graphite carbon server port. Defaults to 2003 if protocol is plaintext, 2004 if protocol is pickle.
			restriction (integer)
				:minimum 0
				:maximum 65535
		:protocol (type=enumeration, min-occurs=0, max-occurs=1) Graphite server protocol. Defaults to pickle.
			restriction (enumeration)
				:value plaintext
				:value pickle
		:server-metrics (type=enumeration, min-occurs=0, max-occurs=9) Predefined Mediaflux server metrics.
			restriction (enumeration)
				:value thread
				:value licence
				:value connection
				:value memory
				:value os
				:value store
				:value stream
				:value task
				:value all

	execution: local
	authority required: ADMINISTER
	can abort: false
```
## 3. List the metrics (without sending)

You can also list the metrics without sending to Graphite.

* Run the following service in Aterm:
  * **`graphite.metrics.list :server-metrics all`**

## 4. Schedule a job to send metrics periodically

Run the follow command in Aterm to send metrics to the specified server every 5 minutes:

* **`schedule.job.create :name Send-Server-Metrics-To-Graphite :service -name graphite.metrics.send < :host your-graphite-server.org > :when < :every -unit minute 5 >`**

## References
  1. http://graphite.readthedocs.io/en/latest/feeding-carbon.html
  2. https://github.com/graphite-project/carbon/blob/master/examples/example-pickle-client.py
  3. https://github.com/pydawan/pickle/blob/master/src/main/java/org/java/serializer/pickle/Pickle.java
  4. http://www.diveintopython3.net/serializing.html
  
