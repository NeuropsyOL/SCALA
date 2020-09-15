/*=============================================================================
    Copyright (c) 2001-2011 Joel de Guzman

    Distributed under the Boost Software License, Version 1.0. (See accompanying 
    file LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)
==============================================================================*/
#if !defined(FUSION_HAS_KEY_10022005_1617)
#define FUSION_HAS_KEY_10022005_1617

#include <lslboost/mpl/has_key.hpp>
#include <lslboost/fusion/sequence/intrinsic/has_key.hpp>

namespace lslboost { namespace mpl
{
    template <typename Tag>
    struct has_key_impl;

    template <>
    struct has_key_impl<fusion::fusion_sequence_tag>
    {
        template <typename Sequence, typename Key>
        struct apply : fusion::result_of::has_key<Sequence, Key> {};
    };
}}

#endif

