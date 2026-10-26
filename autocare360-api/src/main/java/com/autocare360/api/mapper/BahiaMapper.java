package com.autocare360.api.mapper;

import com.autocare360.api.dto.request.BahiaRequest;
import com.autocare360.api.dto.response.BahiaResponse;
import com.autocare360.api.model.Bahia;
import com.autocare360.api.model.enums.EstadoBahia;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class BahiaMapper {

    public Bahia toEntity(BahiaRequest request) {
        return Bahia.builder()
                .nombreBahia(StringUtils.trim(request.nombreBahia()))
                .estado(ObjectUtils.defaultIfNull(request.estado(), EstadoBahia.DISPONIBLE))
                .build();
    }

    public void actualizar(Bahia bahia, BahiaRequest request) {
        bahia.setNombreBahia(StringUtils.trim(request.nombreBahia()));
        if (request.estado() != null) {
            bahia.setEstado(request.estado());
        }
    }

    public BahiaResponse toResponse(Bahia bahia) {
        return new BahiaResponse(bahia.getIdBahia(), bahia.getNombreBahia(), bahia.getEstado());
    }

    public List<BahiaResponse> toResponseList(Collection<Bahia> bahias) {
        return bahias.stream().map(this::toResponse).collect(ImmutableList.toImmutableList());
    }
}