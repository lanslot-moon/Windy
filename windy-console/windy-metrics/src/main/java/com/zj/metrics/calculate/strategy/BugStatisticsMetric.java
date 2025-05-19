package com.zj.metrics.calculate.strategy;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.domain.entity.enums.BugStatus;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.metric.IMetricResultRepository;
import com.zj.metrics.utils.MetricUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BugStatisticsMetric extends BaseMetric{

    private final IBugRepository bugRepository;

    protected BugStatisticsMetric(UniqueIdService uniqueIdService, IMetricResultRepository metricResultRepository,
                                  IBugRepository bugRepository) {
        super(uniqueIdService, metricResultRepository);
        this.bugRepository = bugRepository;
    }

    @Override
    public String getMetricType() {
        return "bug_statistics";
    }

    @Override
    public boolean matchMetric(String category, String calcType) {
        return Objects.equals(category, "bug") && Objects.equals(calcType, "status");
    }

    @Override
    public Integer calculateMetric(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources) {
        List<BugBO> allBugs = bugRepository.getAllNotCompleteBugs();
        Map<Integer, List<BugBO>> statusMap = allBugs.stream().collect(Collectors.groupingBy(BugBO::getStatus));

        List<MetricResultBO> metricResults = statusMap.keySet().stream().map(status -> {
            BugStatus bugStatus = BugStatus.convertType(status);
            if (Objects.isNull(bugStatus)) {
                log.info("can not find Bug status={}", status);
                return null;
            }
            List<BugBO> bugs = statusMap.get(status);
            double count = Optional.ofNullable(bugs).map(List::size).orElse(0);
            return createMetricResult(bugStatus.getDesc(), metricDefinition.getMetricId(), getMetricType(),
                    MetricUtils.scaleTo1Decimal(count));
        }).filter(Objects::nonNull).collect(Collectors.toList());

        MetricResultBO totalMetricResult = createMetricResult(MetricNameType.BUG_TOTAL.getMetricName(),
                metricDefinition.getMetricId(), getMetricType(), (double) allBugs.size());
        metricResults.add(totalMetricResult);
        boolean batchSave = batchSaveMetric(metricResults);
        log.info("batch save bug status metric result={}", batchSave);
        return metricResults.size();
    }
}
