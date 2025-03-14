package org.example.services;

import org.example.clients.ServerClient;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {
    private ServerClient serverClient;

    public StatisticService(ServerClient serverClient){
        this.serverClient = serverClient;
    }

    public String getByWeak() {
        var statisticMono = serverClient.getStatistic();
        var statistic = statisticMono.block();

        return statistic;
    }
}
