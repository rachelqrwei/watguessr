package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.GuessCreateDto;
import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.mapper.GuessMapper;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.service.GuessService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/guess")
public class GuessController {

    @Autowired
    private GuessService guessService;

    @Autowired
    private GuessMapper guessMapper;
//    @PostMapping
//    public void guess(@RequestBody GuessDto guessDto) {
//        guessService.create(guessDto);
//    }

    @PostMapping
    public ResponseEntity<GuessDto> createGuess(
            @RequestBody @Valid GuessCreateDto createDto
    ) {
        // 1. DTO → Entity
        Guess toSave = guessMapper.toEntity(createDto);

        // 2. persist
        Guess saved  = guessService.create(toSave);

        // 3. Entity → full DTO (with generated id + points)
        GuessDto result = guessMapper.toDto(saved);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping(value = "/get-all-guess")
    public List<Guess> getAllGuess(@RequestParam UUID roundId) {
        return guessService.findAllGuessByRoundId(roundId);
    }

}
