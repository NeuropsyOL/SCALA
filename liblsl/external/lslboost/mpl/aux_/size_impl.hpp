
#ifndef BOOST_MPL_AUX_SIZE_IMPL_HPP_INCLUDED
#define BOOST_MPL_AUX_SIZE_IMPL_HPP_INCLUDED

// Copyright Aleksey Gurtovoy 2000-2004
//
// Distributed under the Boost Software License, Version 1.0. 
// (See accompanying file LICENSE_1_0.txt or copy at 
// http://www.lslboost.org/LICENSE_1_0.txt)
//
// See http://www.lslboost.org/libs/mpl for documentation.

// $Id: size_impl.hpp 49267 2008-10-11 06:19:02Z agurtovoy $
// $Date: 2008-10-10 23:19:02 -0700 (Fri, 10 Oct 2008) $
// $Revision: 49267 $

#include <lslboost/mpl/size_fwd.hpp>
#include <lslboost/mpl/begin_end.hpp>
#include <lslboost/mpl/distance.hpp>
#include <lslboost/mpl/aux_/traits_lambda_spec.hpp>
#include <lslboost/mpl/aux_/config/workaround.hpp>

namespace lslboost { namespace mpl {

// default implementation; conrete sequences might override it by 
// specializing either the 'size_impl' or the primary 'size' template

template< typename Tag >
struct size_impl
{
    template< typename Sequence > struct apply
#if !BOOST_WORKAROUND(__BORLANDC__, BOOST_TESTED_AT(0x561))
        : distance<
              typename begin<Sequence>::type
            , typename end<Sequence>::type
            >
    {
#else
    {
        typedef typename distance<
              typename begin<Sequence>::type
            , typename end<Sequence>::type
            >::type type;
#endif
    };
};

BOOST_MPL_ALGORITM_TRAITS_LAMBDA_SPEC(1, size_impl)

}}

#endif // BOOST_MPL_AUX_SIZE_IMPL_HPP_INCLUDED
