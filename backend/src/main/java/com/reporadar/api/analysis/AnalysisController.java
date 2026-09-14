package com.reporadar.api.analysis;

import com.reporadar.security.AuthenticatedUser;
import com.reporadar.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analyses")
public class AnalysisController {
    private final AnalysisService analyses;
    public AnalysisController(AnalysisService analyses) { this.analyses = analyses; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AnalysisDto.AnalysisResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody AnalysisDto.CreateAnalysisRequest request) { return analyses.create(user.id(), request); }
    @GetMapping
    List<AnalysisDto.AnalysisResponse> list(@AuthenticationPrincipal AuthenticatedUser user) { return analyses.list(user.id()); }
    @GetMapping("/{analysisId}")
    AnalysisDto.AnalysisResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID analysisId) { return analyses.get(user.id(), analysisId); }
}
