
#ifndef BOOST_MPL_SINGLE_VIEW_HPP_INCLUDED
#define BOOST_MPL_SINGLE_VIEW_HPP_INCLUDED

// Copyright Aleksey Gurtovoy 2000-2004
//
// Distributed under the Boost Software License, Version 1.0. 
// (See accompanying file LICENSE_1_0.txt or copy at 
// http://www.lslboost.org/LICENSE_1_0.txt)
//
// See http://www.lslboost.org/libs/mpl for documentation.

// $Id: single_view.hpp 49267 2008-10-11 06:19:02Z agurtovoy $
// $Date: 2008-10-10 23:19:02 -0700 (Fri, 10 Oct 2008) $
// $Revision: 49267 $

#include <lslboost/mpl/aux_/single_element_iter.hpp>
#include <lslboost/mpl/iterator_range.hpp>
#include <lslboost/mpl/aux_/na_spec.hpp>

namespace lslboost { namespace mpl {

template<
      typename BOOST_MPL_AUX_NA_PARAM(T)
    >
struct single_view
    : iterator_range<
          aux::sel_iter<T,0>
        , aux::sel_iter<T,1>
        >
{
};

BOOST_MPL_AUX_NA_SPEC_NO_ETI(1, single_view)

}}

#endif // BOOST_MPL_SINGLE_VIEW_HPP_INCLUDED
