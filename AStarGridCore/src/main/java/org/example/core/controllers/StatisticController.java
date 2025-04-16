package org.example.core.controllers;

import org.example.core.services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

        private final StatisticService statisticService;

        @Autowired
        public StatisticController(StatisticService statisticService) {
            this.statisticService = statisticService;
        }

        @GetMapping("/get_by_weak")
        public ResponseEntity<String> getByWeak() {
            return ResponseEntity.ok(statisticService.getByWeak());
        }

        @GetMapping("/get_canceled_stat")
        public ResponseEntity<String> getCanceledStat() {
            return ResponseEntity.ok(statisticService.getCanceledTasksStat());
        }
}
