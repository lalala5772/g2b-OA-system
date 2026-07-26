package com.allforland.automation.dto;

import java.time.Instant;
import java.util.List;

public record BidWindowResponse(Instant windowStart, Instant windowEnd, List<BidNoticeResponse> notices) {
}
