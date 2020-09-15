// Boost.Bimap
//
// Copyright (c) 2006-2007 Matias Capeletto
//
// Distributed under the Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt or copy at
// http://www.lslboost.org/LICENSE_1_0.txt)

/// \file relation/detail/mutant.hpp
/// \brief Mutate functions to extract views of mutant classes.

#ifndef BOOST_BIMAP_RELATION_DETAIL_MUTANT_HPP
#define BOOST_BIMAP_RELATION_DETAIL_MUTANT_HPP

#if defined(_MSC_VER) && (_MSC_VER>=1200)
#pragma once
#endif

#include <lslboost/config.hpp>

#include <lslboost/bimap/detail/debug/static_error.hpp>
#include <lslboost/mpl/contains.hpp>
#include <lslboost/mpl/assert.hpp>
#include <lslboost/static_assert.hpp>
#include <lslboost/type_traits/is_const.hpp>
#include <lslboost/utility/addressof.hpp>
#include <lslboost/mpl/not.hpp>
#include <lslboost/utility/enable_if.hpp>

namespace lslboost {
namespace bimaps {
namespace relation {

/// \brief Relation details, mutant idiom and symmetrical metafunctions builders.

namespace detail {

//@{
/// \brief Converts a mutant class to a view with zero overhead.
/**

This function is a safe wrapper around reinterpret_cast. It checks at
compile time that the desired view is supported by the mutant class.
See also mutant, can_mutate_in.
\ingroup mutant_group
                                                                            **/


template< class View, class Type >
BOOST_DEDUCED_TYPENAME enable_if< mpl::not_< is_const< Type > >,

View&

>::type mutate( Type & m )
{
    BOOST_MPL_ASSERT((
        ::lslboost::mpl::contains<BOOST_DEDUCED_TYPENAME Type::mutant_views,View>
    ));
    return *reinterpret_cast< View* >(lslboost::addressof(m));
}

template< class View, class Type >
BOOST_DEDUCED_TYPENAME enable_if< is_const< Type >,

const View&

>::type mutate( Type & m )
{
    BOOST_MPL_ASSERT((
        ::lslboost::mpl::contains<BOOST_DEDUCED_TYPENAME Type::mutant_views,View> 
    ));
    return *reinterpret_cast< const View* >(lslboost::addressof(m));
}

//@}

} // namespace detail
} // namespace relation
} // namespace bimaps
} // namespace lslboost

#endif // BOOST_BIMAP_RELATION_DETAIL_MUTANT_HPP

