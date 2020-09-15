/*=============================================================================
    Copyright (c) 2001-2011 Joel de Guzman

    Distributed under the Boost Software License, Version 1.0. (See accompanying 
    file LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)
==============================================================================*/
#if !defined(FUSION_IS_ITERATOR_05062005_1219)
#define FUSION_IS_ITERATOR_05062005_1219

#include <lslboost/type_traits/is_base_of.hpp>

namespace lslboost { namespace fusion
{
    struct iterator_root;

    template <typename T>
    struct is_fusion_iterator : is_base_of<iterator_root, T> {};
}}

#endif
