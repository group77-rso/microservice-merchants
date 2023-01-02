package com.merchants.models.converters;

import com.merchants.lib.Merchant;
import com.merchants.lib.Price;
import com.merchants.models.entities.MerchantsEntity;
import com.merchants.models.entities.PriceEntity;

import java.util.HashSet;
import java.util.Set;

public class MerchantsConverter {


    public static Merchant toDto(MerchantsEntity entity, boolean withPrices) {
        Merchant merchant = toDto(entity);
        if (withPrices) {
            merchant.setPrices(getPriceFromPriceEntity(entity.getPrices()));
        }
        return merchant;
    }

    public static Merchant toDto(MerchantsEntity entity) {

        Merchant dto = new Merchant();
        dto.setMerchantId(entity.getId());
        dto.setName(entity.getName());
        dto.setLogoUrl(entity.getLogoUrl());

        return dto;
    }

    public static MerchantsEntity toEntity(Merchant dto) {

        MerchantsEntity entity = new MerchantsEntity();
        entity.setName(dto.getName());
        entity.setId(dto.getMerchantId());

        return entity;
    }

    private static Set<Price> getPriceFromPriceEntity(Set<PriceEntity> priceEntitySet) {
        Set<Price> prices = new HashSet<>();
        for (PriceEntity priceEntity : priceEntitySet) {
            prices.add(PriceConverter.toDto(priceEntity));
        }
        return prices;
    }

}
