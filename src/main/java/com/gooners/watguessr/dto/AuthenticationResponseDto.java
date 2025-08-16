package com.gooners.watguessr.dto;

public record AuthenticationResponseDto(String token, UserDto user) {
}