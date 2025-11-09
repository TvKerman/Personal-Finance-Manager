package com.finance.infra.export;

import com.finance.core.model.Wallet;
import java.io.IOException;

public interface ReportExporter {
    void export(Wallet wallet, String filename) throws IOException;
}