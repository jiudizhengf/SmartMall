package org.example.smartmallbackend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductOnShelfEvent extends ApplicationEvent {
    private final Long spuId;
    public ProductOnShelfEvent(Object source, Long spuId) {
        super(source);
        this.spuId = spuId;
    }
}
