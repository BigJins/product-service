// 1) Redis Sentinel 전용 프로퍼티 바인딩
package org.allmart.productservice.infrastructure.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.data.redis.sentinel")
public class StockRedisProperties {
    /** master 이름 ("mymaster") */
    private String master;
    /** ["sentinel-1:26379","sentinel-2:26379","sentinel-3:26379"] */
    private List<String> nodes = new ArrayList<>();

    public String getMaster() { return master; }
    public void setMaster(String master) { this.master = master; }

    public List<String> getNodes() { return nodes; }
    public void setNodes(List<String> nodes) { this.nodes = nodes; }
}