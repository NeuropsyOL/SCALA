
#ifndef BOOST_MPL_ITER_FOLD_BACKWARD_HPP_INCLUDED
#define BOOST_MPL_ITER_FOLD_BACKWARD_HPP_INCLUDED

// Copyright Aleksey Gurtovoy 2001-2004
// Copyright Dave Abrahams 2001-2002
//
// Distributed under the Boost Software License, Version 1.0. 
// (See accompanying file LICENSE_1_0.txt or copy at 
// http://www.lslboost.org/LICENSE_1_0.txt)
//
// See http://www.lslboost.org/libs/mpl for documentation.

// $Id: reverse_iter_fold.hpp 49267 2008-10-11 06:19:02Z agurtovoy $
// $Date: 2008-10-10 23:19:02 -0700 (Fri, 10 Oct 2008) $
// $Revision: 49267 $

#include <lslboost/mpl/begin_end.hpp>
#include <lslboost/mpl/O1_size.hpp>
#include <lslboost/mpl/arg.hpp>
#include <lslboost/mpl/lambda.hpp>
#include <lslboost/mpl/aux_/reverse_iter_fold_impl.hpp>
#include <lslboost/mpl/aux_/na_spec.hpp>
#include <lslboost/mpl/aux_/lambda_support.hpp>

namespace lslboost { namespace mpl {

template<
      typename BOOST_MPL_AUX_NA_PARAM(Sequence)
    , typename BOOST_MPL_AUX_NA_PARAM(State)
    , typename BOOST_MPL_AUX_NA_PARAM(BackwardOp)
    , typename ForwardOp = arg<1>
    >
struct reverse_iter_fold
{
    typedef typename aux::reverse_iter_fold_impl<
          ::lslboost::mpl::O1_size<Sequence>::value
        , typename begin<Sequence>::type
        , typename end<Sequence>::type
        , State
        , typename lambda<BackwardOp>::type
        , typename lambda<ForwardOp>::type
        >::state type;

    BOOST_MPL_AUX_LAMBDA_SUPPORT(
          4
        , reverse_iter_fold
        , (Sequence,State,BackwardOp,ForwardOp)
        )
};

BOOST_MPL_AUX_NA_SPEC(3, reverse_iter_fold)

}}

#endif // BOOST_MPL_ITER_FOLD_BACKWARD_HPP_INCLUDED
