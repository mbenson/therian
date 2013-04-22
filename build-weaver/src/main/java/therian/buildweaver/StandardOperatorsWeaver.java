package therian.buildweaver;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.weaver.model.ScanRequest;
import org.apache.commons.weaver.model.ScanResult;
import org.apache.commons.weaver.spi.Weaver;

public class StandardOperatorsWeaver implements Weaver {

    @Override
    public void configure(List<String> classPath, File target, Properties config) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ScanRequest getScanRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean process(ScanResult scanResult) {
        // TODO Auto-generated method stub
        return false;
    }

}
