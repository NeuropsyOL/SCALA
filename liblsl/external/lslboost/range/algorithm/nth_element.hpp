//  Copyright Neil Groves 2009. Use, modification and
//  distribution is subject to the Boost Software License, Version
//  1.0. (See accompanying file LICENSE_1_0.txt or copy at
//  http://www.lslboost.org/LICENSE_1_0.txt)
//
//
// For more information, see http://www.lslboost.org/libs/range/
//
#ifndef BOOST_RANGE_ALGORITHM_NTH_ELEMENT_HPP_INCLUDED
#define BOOST_RANGE_ALGORITHM_NTH_ELEMENT_HPP_INCLUDED

#include <lslboost/concept_check.hpp>
#include <lslboost/range/begin.hpp>
#include <lslboost/range/end.hpp>
#include <lslboost/range/concepts.hpp>
#include <algorithm>

namespace lslboost
{
    namespace range
    {

/// \brief template function nth_element
///
/// range-based version of the nth_element std algorithm
///
/// \pre RandomAccessRange is a model of the RandomAccessRangeConcept
/// \pre BinaryPredicate is a model of the BinaryPredicateConcept
template<class RandomAccessRange>
inline RandomAccessRange& nth_element(RandomAccessRange& rng,
    BOOST_DEDUCED_TYPENAME range_iterator<RandomAccessRange>::type nth)
{
    BOOST_RANGE_CONCEPT_ASSERT(( RandomAccessRangeConcept<RandomAccessRange> ));
    std::nth_element(lslboost::begin(rng), nth, lslboost::end(rng));
    return rng;
}

/// \overload
template<class RandomAccessRange>
inline const RandomAccessRange& nth_element(const RandomAccessRange& rng,
    BOOST_DEDUCED_TYPENAME range_iterator<const RandomAccessRange>::type nth)
{
    BOOST_RANGE_CONCEPT_ASSERT(( RandomAccessRangeConcept<const RandomAccessRange> ));
    std::nth_element(lslboost::begin(rng), nth, lslboost::end(rng));
    return rng;
}

/// \overload
template<class RandomAccessRange, class BinaryPredicate>
inline RandomAccessRange& nth_element(RandomAccessRange& rng,
    BOOST_DEDUCED_TYPENAME range_iterator<RandomAccessRange>::type nth,
    BinaryPredicate sort_pred)
{
    BOOST_RANGE_CONCEPT_ASSERT(( RandomAccessRangeConcept<RandomAccessRange> ));
    std::nth_element(lslboost::begin(rng), nth, lslboost::end(rng), sort_pred);
    return rng;
}

/// \overload
template<class RandomAccessRange, class BinaryPredicate>
inline const RandomAccessRange& nth_element(const RandomAccessRange& rng,
    BOOST_DEDUCED_TYPENAME range_iterator<const RandomAccessRange>::type nth,
    BinaryPredicate sort_pred)
{
    BOOST_RANGE_CONCEPT_ASSERT(( RandomAccessRangeConcept<const RandomAccessRange> ));
    std::nth_element(lslboost::begin(rng), nth, lslboost::end(rng), sort_pred);
    return rng;
}

    } // namespace range
    using range::nth_element;
} // namespace lslboost

#endif // include guard
