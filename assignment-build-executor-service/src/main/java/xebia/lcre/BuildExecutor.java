package xebia.lcre;

import xebia.lcre.build.executors.LambdaBuildExecutor;
import xebia.lcre.build.result.BuildResult;
import xebia.lcre.build.spec.BuildSpecificationBuilder;
import xebia.lcre.pkg.creators.BuildPackage;

public interface BuildExecutor {

    static BuildExecutor newRunner(BuildSpecificationBuilder buildSpecificationBuilder) {
        return new LambdaBuildExecutor(buildSpecificationBuilder);
    }

    BuildResult execute(BuildPackage buildPackage);

}
