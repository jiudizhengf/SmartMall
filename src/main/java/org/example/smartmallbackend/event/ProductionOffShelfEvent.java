package org.example.smartmallbackend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductionOffShelfEvent extends ApplicationEvent {
    private final Long spuId;

    public ProductionOffShelfEvent(Object source, Long spuId) {
        super(source);
        this.spuId = spuId;
    }
}
