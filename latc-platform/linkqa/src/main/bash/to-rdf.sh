#!/bin/bash

id="$1"
params="$2"
polarity="$3"

DIR="$( cd "$( dirname "$0" )" && pwd )"

source "$params"

source "$DIR/config-$polarity.ini"

echo "<$1> <$p_precision> \"$precision\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_recall> \"$recall\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_estimatedPrecisionLowerBound> \"$estimatedPrecisionLowerBound\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_estimatedPrecisionUpperBound> \"$estimatedPrecisionLowerBound\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_rawRefsetSize> \"$rawRefsetSize\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_intersectionSize> \"$intersectionSize\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_rawLinksetSize> \"$rawLinksetSize\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_linksetErrorCount> \"$linksetErrorCount\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_refsetDuplicateSize> \"$refsetDuplicateSize\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_linksetDuplicateSize> \"$linksetDuplicateSize\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_refsetErrorCount> \"$refsetErrorCount\"^^<http://www.w3.org/2001/XMLSchema#long> ."


