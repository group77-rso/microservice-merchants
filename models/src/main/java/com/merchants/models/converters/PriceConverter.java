package com.merchants.models.converters;

import com.merchants.lib.Price;
import com.merchants.models.entities.PriceEntity;

public class PriceConverter {


    public static Price toDto(PriceEntity entity) {

        Price dto = new Price();
        dto.setProductId(entity.getProductId());
        dto.setMerchant(entity.getMerchant().getName());
        dto.setPrice(entity.getPrice());

        return dto;
    }

}
