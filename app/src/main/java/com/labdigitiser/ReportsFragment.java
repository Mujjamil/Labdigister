package com.labdigitiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.labdigitiser.network.WebsiteRepository;
import com.labdigitiser.network.model.ApiDashboardData;
import com.labdigitiser.network.model.ApiDashboardEntry;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiReadingDetail;
import com.labdigitiser.network.model.ApiReadingValue;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiStats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {

    private static final String RANGE_ALL = "all";
    private static final String RANGE_TODAY = "today";
    private static final String RANGE_WEEK = "week";
    private static final String RANGE_MONTH = "month";

    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM dd", Locale.US);
    private final SimpleDateFormat syncTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

    private WebsiteRepository websiteRepository;
    private TextView plantText;
    private TextView syncBadgeText;
    private TextView statusText;
    private TextView totalEntriesValueText;
    private TextView scopeLabelText;
    private TextView scopeValueText;
    private TextView avgPhValueText;
    private TextView avgTdsValueText;
    private TextView emptyStateText;
    private LinearLayout rowsContainer;
    private MaterialButton exportButton;
    private TextView recordsActionText;
    private TextView chipAll;
    private TextView chipToday;
    private TextView chipWeek;
    private TextView chipMonth;

    private String selectedRange = RANGE_ALL;
    private ApiStats latestStats;
    private final List<ReportRow> reportRows = new ArrayList<>();
    private int pendingRowRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        websiteRepository = new WebsiteRepository(requireContext());
        apiDateFormat.setLenient(false);
        bindViews(view);
        bindActions();
        loadReportContext();
        return view;
    }

    private void bindViews(View view) {
        plantText = view.findViewById(R.id.tv_export_plant_name);
        syncBadgeText = view.findViewById(R.id.tv_export_sync_badge);
        statusText = view.findViewById(R.id.tv_export_status);
        totalEntriesValueText = view.findViewById(R.id.tv_stat_total_entries);
        scopeLabelText = view.findViewById(R.id.tv_stat_scope_label);
        scopeValueText = view.findViewById(R.id.tv_stat_today_entries);
        avgPhValueText = view.findViewById(R.id.tv_stat_avg_ph);
        avgTdsValueText = view.findViewById(R.id.tv_stat_avg_tds);
        rowsContainer = view.findViewById(R.id.layout_export_rows);
        emptyStateText = view.findViewById(R.id.tv_export_empty);
        exportButton = view.findViewById(R.id.button_download_export);
        recordsActionText = view.findViewById(R.id.tv_records_action);
        chipAll = view.findViewById(R.id.chip_export_all);
        chipToday = view.findViewById(R.id.chip_export_today);
        chipWeek = view.findViewById(R.id.chip_export_week);
        chipMonth = view.findViewById(R.id.chip_export_month);
    }

    private void bindActions() {
        exportButton.setOnClickListener(v -> onExportClicked());
        recordsActionText.setOnClickListener(v -> onExportClicked());
        chipAll.setOnClickListener(v -> selectRange(RANGE_ALL));
        chipToday.setOnClickListener(v -> selectRange(RANGE_TODAY));
        chipWeek.setOnClickListener(v -> selectRange(RANGE_WEEK));
        chipMonth.setOnClickListener(v -> selectRange(RANGE_MONTH));
    }

    private void loadReportContext() {
        statusText.setText("Loading report data...");
        syncBadgeText.setText("Live API");
        exportButton.setEnabled(false);
        websiteRepository.getCurrentUser().enqueue(new Callback<ApiResponse<ApiSessionData>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiSessionData>> call, Response<ApiResponse<ApiSessionData>> response) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<ApiSessionData> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    statusText.setText(body != null ? body.getReadableMessage() : "Unable to load report data.");
                    return;
                }

                ApiPlant plant = getSelectedPlant(body.getData().getPlants());
                if (plant == null) {
                    plantText.setText("No Plant");
                    statusText.setText("Assign or select a plant to view reports.");
                    return;
                }

                websiteRepository.selectPlant(String.valueOf(plant.getId()), plant.getPlantName());
                plantText.setText(plant.getPlantName() + " / " + plant.getCompanyName());
                loadDashboardData();
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiSessionData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                statusText.setText(t.getMessage());
            }
        });
    }

    private void loadDashboardData() {
        websiteRepository.getDashboard().enqueue(new Callback<ApiResponse<ApiDashboardData>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiDashboardData>> call, Response<ApiResponse<ApiDashboardData>> response) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<ApiDashboardData> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    statusText.setText(body != null ? body.getReadableMessage() : "Unable to load dashboard data.");
                    return;
                }

                latestStats = body.getData().getStats();
                syncBadgeText.setText("Updated . " + syncTimeFormat.format(new Date()));
                renderOverviewStats();
                loadRecentRows(body.getData().getRecentEntries());
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiDashboardData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                statusText.setText(t.getMessage());
            }
        });
    }

    private void loadRecentRows(List<ApiDashboardEntry> entries) {
        reportRows.clear();
        rowsContainer.removeAllViews();
        emptyStateText.setVisibility(View.GONE);

        if (entries == null || entries.isEmpty()) {
            renderOverviewStats();
            renderRows();
            statusText.setText("No recent records available for this plant.");
            return;
        }

        int limit = Math.min(entries.size(), 6);
        pendingRowRequests = limit;
        statusText.setText("Loading recent report rows...");

        for (int i = 0; i < limit; i++) {
            ApiDashboardEntry entry = entries.get(i);
            final int rowIndex = i;
            websiteRepository.getReading(String.valueOf(entry.getId())).enqueue(new Callback<ApiResponse<ApiReadingDetail>>() {
                @Override
                public void onResponse(Call<ApiResponse<ApiReadingDetail>> call, Response<ApiResponse<ApiReadingDetail>> response) {
                    if (!isAdded()) {
                        return;
                    }

                    ApiResponse<ApiReadingDetail> body = response.body();
                    if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                        reportRows.add(buildRowFromDetail(rowIndex, body.getData()));
                    } else {
                        reportRows.add(buildFallbackRow(rowIndex, entry));
                    }
                    onRowLoaded();
                }

                @Override
                public void onFailure(Call<ApiResponse<ApiReadingDetail>> call, Throwable t) {
                    if (!isAdded()) {
                        return;
                    }
                    reportRows.add(buildFallbackRow(rowIndex, entry));
                    onRowLoaded();
                }
            });
        }
    }

    private void onRowLoaded() {
        pendingRowRequests--;
        if (pendingRowRequests > 0) {
            return;
        }

        Collections.sort(reportRows, Comparator.comparingInt(row -> row.index));
        renderOverviewStats();
        renderRows();
        statusText.setText("Report view ready. Export action will use the selected range once the API route is available.");
    }

    private void selectRange(String range) {
        selectedRange = range;
        updateChipStyles();
        renderOverviewStats();
        renderRows();
    }

    private void updateChipStyles() {
        styleChip(chipAll, RANGE_ALL.equals(selectedRange));
        styleChip(chipToday, RANGE_TODAY.equals(selectedRange));
        styleChip(chipWeek, RANGE_WEEK.equals(selectedRange));
        styleChip(chipMonth, RANGE_MONTH.equals(selectedRange));
    }

    private void styleChip(TextView chip, boolean active) {
        chip.setBackgroundResource(active ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        chip.setTextColor(ContextCompat.getColor(requireContext(), active ? android.R.color.white : R.color.text_muted));
    }

    private void renderOverviewStats() {
        int totalEntries = latestStats != null ? latestStats.getTotalRecords() : 0;
        totalEntriesValueText.setText(String.valueOf(totalEntries));

        scopeLabelText.setText(getScopeLabel());
        scopeValueText.setText(String.valueOf(getScopeCount()));

        List<ReportRow> filteredRows = getFilteredRows();
        avgPhValueText.setText(formatAverage(extractAverage(filteredRows, true)));
        avgTdsValueText.setText(formatAverage(extractAverage(filteredRows, false)));

        exportButton.setEnabled(getScopeCount() > 0);
        exportButton.setText("Export");
    }

    private void renderRows() {
        rowsContainer.removeAllViews();
        List<ReportRow> filteredRows = getFilteredRows();
        if (filteredRows.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No records available for " + getReadableRange().toLowerCase(Locale.US) + ".");
            return;
        }

        emptyStateText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ReportRow row : filteredRows) {
            View rowView = inflater.inflate(R.layout.include_report_record_row, rowsContainer, false);
            TextView dateText = rowView.findViewById(R.id.tv_row_date);
            TextView locationText = rowView.findViewById(R.id.tv_row_location);
            TextView phText = rowView.findViewById(R.id.tv_row_ph);
            TextView tdsText = rowView.findViewById(R.id.tv_row_tds);
            TextView statusRowText = rowView.findViewById(R.id.tv_row_status);

            dateText.setText(row.displayDate);
            locationText.setText(row.location);
            phText.setText(row.phText);
            tdsText.setText(row.tdsText);
            statusRowText.setText("SYNCED");

            rowsContainer.addView(rowView);
        }
    }

    private void onExportClicked() {
        int count = getScopeCount();
        if (count <= 0) {
            Toast.makeText(requireContext(), "No rows available for export.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(
                requireContext(),
                "Export UI is ready. Backend CSV API is still needed for " + getReadableRange().toLowerCase(Locale.US) + ".",
                Toast.LENGTH_LONG
        ).show();
        statusText.setText("Export button is mapped to the selected range, but the backend CSV route is still pending.");
    }

    private ApiPlant getSelectedPlant(List<ApiPlant> plants) {
        if (plants == null || plants.isEmpty()) {
            return null;
        }

        String selectedPlantId = websiteRepository.getSelectedPlantId();
        if (selectedPlantId != null) {
            for (ApiPlant plant : plants) {
                if (selectedPlantId.equals(String.valueOf(plant.getId()))) {
                    return plant;
                }
            }
        }

        return plants.get(0);
    }

    private ReportRow buildRowFromDetail(int index, ApiReadingDetail detail) {
        return new ReportRow(
                index,
                detail.getReadingDate(),
                formatDate(detail.getReadingDate()),
                valueOrFallback(detail.getLocationName(), "-"),
                formatNumber(findMetricValue(detail.getValues(), "ph")),
                formatInteger(findMetricValue(detail.getValues(), "tds"))
        );
    }

    private ReportRow buildFallbackRow(int index, ApiDashboardEntry entry) {
        return new ReportRow(
                index,
                entry.getReadingDate(),
                formatDate(entry.getReadingDate()),
                valueOrFallback(entry.getLocation(), "-"),
                "--",
                "--"
        );
    }

    private double findMetricValue(List<ApiReadingValue> values, String key) {
        if (values == null) {
            return Double.NaN;
        }

        for (ApiReadingValue value : values) {
            String name = value.getParameterName();
            if (name != null && name.toLowerCase(Locale.US).contains(key.toLowerCase(Locale.US))) {
                return value.getValue();
            }
        }
        return Double.NaN;
    }

    private List<ReportRow> getFilteredRows() {
        if (reportRows.isEmpty()) {
            return new ArrayList<>();
        }

        if (RANGE_ALL.equals(selectedRange)) {
            return new ArrayList<>(reportRows);
        }

        List<ReportRow> filtered = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        for (ReportRow row : reportRows) {
            Date parsedDate = parseApiDate(row.rawDate);
            if (parsedDate == null) {
                if (RANGE_ALL.equals(selectedRange)) {
                    filtered.add(row);
                }
                continue;
            }

            Calendar rowCal = Calendar.getInstance();
            rowCal.setTime(parsedDate);
            if (RANGE_TODAY.equals(selectedRange) && isSameDay(now, rowCal)) {
                filtered.add(row);
            } else if (RANGE_WEEK.equals(selectedRange) && isWithinLastSevenDays(now, rowCal)) {
                filtered.add(row);
            } else if (RANGE_MONTH.equals(selectedRange)
                    && now.get(Calendar.YEAR) == rowCal.get(Calendar.YEAR)
                    && now.get(Calendar.MONTH) == rowCal.get(Calendar.MONTH)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isWithinLastSevenDays(Calendar now, Calendar row) {
        Calendar start = (Calendar) now.clone();
        start.add(Calendar.DAY_OF_YEAR, -6);
        zeroTime(start);
        Calendar candidate = (Calendar) row.clone();
        zeroTime(candidate);
        Calendar end = (Calendar) now.clone();
        zeroTime(end);
        return !candidate.before(start) && !candidate.after(end);
    }

    private void zeroTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private int getScopeCount() {
        if (latestStats == null) {
            return getFilteredRows().size();
        }

        switch (selectedRange) {
            case RANGE_TODAY:
                return latestStats.getTodayEntries();
            case RANGE_WEEK:
                return latestStats.getWeekEntries();
            case RANGE_MONTH:
                return getFilteredRows().size();
            case RANGE_ALL:
            default:
                return latestStats.getTotalRecords();
        }
    }

    private String getScopeLabel() {
        switch (selectedRange) {
            case RANGE_TODAY:
                return "TODAY COUNT";
            case RANGE_WEEK:
                return "WEEK COUNT";
            case RANGE_MONTH:
                return "MONTH ROWS";
            case RANGE_ALL:
            default:
                return "CURRENT FILTER";
        }
    }

    private String getReadableRange() {
        switch (selectedRange) {
            case RANGE_TODAY:
                return "Today";
            case RANGE_WEEK:
                return "This Week";
            case RANGE_MONTH:
                return "This Month";
            case RANGE_ALL:
            default:
                return "All Records";
        }
    }

    private double extractAverage(List<ReportRow> rows, boolean phMetric) {
        double total = 0;
        int count = 0;
        for (ReportRow row : rows) {
            String value = phMetric ? row.phText : row.tdsText;
            if ("--".equals(value)) {
                continue;
            }
            try {
                total += Double.parseDouble(value);
                count++;
            } catch (NumberFormatException ignored) {
                // Ignore malformed value.
            }
        }
        return count == 0 ? Double.NaN : total / count;
    }

    private String formatAverage(double value) {
        if (Double.isNaN(value)) {
            return "--";
        }
        return value >= 100 ? String.valueOf((int) Math.round(value)) : String.format(Locale.US, "%.1f", value);
    }

    private String formatNumber(double value) {
        if (Double.isNaN(value)) {
            return "--";
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private String formatInteger(double value) {
        if (Double.isNaN(value)) {
            return "--";
        }
        return String.valueOf((int) Math.round(value));
    }

    private Date parseApiDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }

        try {
            return apiDateFormat.parse(rawDate);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private String formatDate(String rawDate) {
        Date parsed = parseApiDate(rawDate);
        return parsed == null ? "--" : shortDateFormat.format(parsed);
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static class ReportRow {
        final int index;
        final String rawDate;
        final String displayDate;
        final String location;
        final String phText;
        final String tdsText;

        ReportRow(int index, String rawDate, String displayDate, String location, String phText, String tdsText) {
            this.index = index;
            this.rawDate = rawDate;
            this.displayDate = displayDate;
            this.location = location;
            this.phText = phText;
            this.tdsText = tdsText;
        }
    }
}
