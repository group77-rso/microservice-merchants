package com.example.merchants.models.converters;

import com.example.merchants.lib.Merchant;
import com.example.merchants.models.entities.MerchantsEntity;

public class MerchantsConverter {

    public static Merchant toDto(MerchantsEntity entity) {

        Merchant dto = new Merchant();
        dto.setMerchantId(entity.getId());
        dto.setName(entity.getTitle());

        return dto;

    }

    public static MerchantsEntity toEntity(Merchant dto) {

        MerchantsEntity entity = new MerchantsEntity();
        entity.setTitle(dto.getName());

        return entity;

    }

}
