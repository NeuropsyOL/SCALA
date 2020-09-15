//  Copyright Neil Groves 2009. Use, modification and
//  distribution is subject to the Boost Software License, Version
//  1.0. (See accompanying file LICENSE_1_0.txt or copy at
//  http://www.lslboost.org/LICENSE_1_0.txt)
//
//
// For more information, see http://www.lslboost.org/libs/range/
//
#ifndef BOOST_RANGE_ALGORITHM_BINARY_SEARCH_HPP_INCLUDED
#define BOOST_RANGE_ALGORITHM_BINARY_SEARCH_HPP_INCLUDED

#include <lslboost/concept_check.hpp>
#include <lslboost/range/begin.hpp>
#include <lslboost/range/end.hpp>
#include <lslboost/range/concepts.hpp>
#include <algorithm>

namespace lslboost
{
    namespace range
    {

/// \brief template function binary_search
///
/// range-based version of the binary_search std algorithm
///
/// \pre ForwardRange is a model of the ForwardRangeConcept
/// \pre BinaryPredicate is a model of the BinaryPredicateConcept
template<class ForwardRange, class Value>
inline bool binary_search(const ForwardRange& rng, const Value& val)
{
    BOOST_RANGE_CONCEPT_ASSERT(( ForwardRangeConcept<const ForwardRange> ));
    return std::binary_search(lslboost::begin(rng), lslboost::end(rng), val);
}

/// \overload
template<class ForwardRange, class Value, class BinaryPredicate>
inline bool binary_search(const ForwardRange& rng, const Value& val,
                          BinaryPredicate pred)
{
    BOOST_RANGE_CONCEPT_ASSERT(( ForwardRangeConcept<const ForwardRange> ));
    return std::binary_search(lslboost::begin(rng), lslboost::end(rng), val, pred);
}

    } // namespace range
    using range::binary_search;
} // namespace lslboost

#endif // include guard
