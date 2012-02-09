#!/bin/bash

id="$1"
params="$2"
polarity="$3"

source "$params"

source "config-$polarity.ini"

echo "<$1> <$p_precision> \"$precision\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_recall> \"$recall\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_estimatedPrecisionLowerBound> \"$estimatedPrecisionLowerBound\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_estimatedPrecisionUpperBound> \"$estimatedPrecisionLowerBound\"^^<http://www.w3.org/2001/XMLSchema#float> ."
echo "<$1> <$p_endDate> \"$rawLinksetSize\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_rawLinksetSize> \"$rawLinksetSize\"^^<http://www.w3.org/2001/XMLSchema#long> ."
echo "<$1> <$p_linksetErrorCount> \"$linksetErrorCount\"^^<http://www.w3.org/2001/XMLSchema#long> ."


