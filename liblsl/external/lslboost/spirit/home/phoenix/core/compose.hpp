/*=============================================================================
    Copyright (c) 2001-2007 Joel de Guzman

    Distributed under the Boost Software License, Version 1.0. (See accompanying 
    file LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)
==============================================================================*/
#ifndef PHOENIX_CORE_COMPOSE_HPP
#define PHOENIX_CORE_COMPOSE_HPP

#include <lslboost/mpl/eval_if.hpp>
#include <lslboost/mpl/identity.hpp>
#include <lslboost/type_traits/is_same.hpp>
#include <lslboost/preprocessor/repetition/enum.hpp>
#include <lslboost/preprocessor/repetition/enum_params.hpp>
#include <lslboost/preprocessor/repetition/enum_params_with_a_default.hpp>
#include <lslboost/spirit/home/phoenix/core/composite.hpp>
#include <lslboost/spirit/home/phoenix/core/value.hpp>
#include <lslboost/spirit/home/phoenix/core/as_actor.hpp>

#define PHOENIX_AS_ACTOR(z, n, data)                                            \
    typename mpl::eval_if<                                                      \
        is_same<BOOST_PP_CAT(T, n), fusion::void_>                              \
      , mpl::identity<fusion::void_>                                            \
      , as_actor_base<BOOST_PP_CAT(T, n)>                                       \
    >::type

namespace lslboost { namespace phoenix
{

///////////////////////////////////////////////////////////////////////////////
//
//  as_composite<EvalPolicy, T0,... TN> metafunction
//
//      Create a composite given an EvalPolicy and types T0..TN.
//      The types are converted to an actor through the as_actor
//      metafunction (see as_actor.hpp).
//
///////////////////////////////////////////////////////////////////////////////
    template <
        typename EvalPolicy
      , BOOST_PP_ENUM_PARAMS_WITH_A_DEFAULT(
            PHOENIX_COMPOSITE_LIMIT, typename T, fusion::void_)>
    struct as_composite
    {
        typedef composite<
            EvalPolicy
          , fusion::vector<
                BOOST_PP_ENUM(PHOENIX_COMPOSITE_LIMIT, PHOENIX_AS_ACTOR, _)>
        >
        type;
    };

///////////////////////////////////////////////////////////////////////////////
//
//  compose functions
//
//      Usage:
//
//          compose<EvalPolicy>(_0, _1,... _N)
//
//      Returns a composite given an EvalPolicy and arguments _0.._N. 
//      The arguments are converted to an actor through the as_actor
//      metafunction (see as_actor.hpp).
//
///////////////////////////////////////////////////////////////////////////////
    template <typename EvalPolicy>
    inline actor<typename as_composite<EvalPolicy>::type>
    compose()
    {
        return actor<typename as_composite<EvalPolicy>::type>();
    }

    template <typename EvalPolicy, typename T0>
    inline actor<typename as_composite<EvalPolicy, T0>::type>
    compose(T0 const& _0)
    {
        return actor<typename as_composite<EvalPolicy, T0>::type>(
            as_actor<T0>::convert(_0)
        );
    }

    template <typename EvalPolicy, typename T0, typename T1>
    inline actor<typename as_composite<EvalPolicy, T0, T1>::type>
    compose(T0 const& _0, T1 const& _1)
    {
        return actor<typename as_composite<EvalPolicy, T0, T1>::type>(
            as_actor<T0>::convert(_0)
          , as_actor<T1>::convert(_1)
        );
    }

    //  Bring in the the rest of the compose overloads
    #include <lslboost/spirit/home/phoenix/core/detail/compose.hpp>

///////////////////////////////////////////////////////////////////////////////
//
//  re_curry<EvalPolicy, T0,...TN>
//
//      returns the result of re currying T0..TN using EvalPolicy.
//
///////////////////////////////////////////////////////////////////////////////
    template <
        typename EvalPolicy
      , BOOST_PP_ENUM_PARAMS_WITH_A_DEFAULT(
            PHOENIX_COMPOSITE_LIMIT, typename T, fusion::void_)>
    struct re_curry
    {
        typedef actor<
            typename as_composite<
                EvalPolicy
              , BOOST_PP_ENUM_PARAMS(PHOENIX_COMPOSITE_LIMIT, T)>::type
            >
        type;
    };
}}

#undef PHOENIX_AS_ACTOR
#endif
