package com.example.smartbudgetpro.ui.investment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbudgetpro.databinding.FragmentInvestmentBinding
import com.example.smartbudgetpro.ui.adapter.InvestmentAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InvestmentFragment : Fragment() {

    private var _binding: FragmentInvestmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InvestmentViewModel by viewModels()
    private lateinit var adapter: InvestmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvestmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InvestmentAdapter { item ->
            // Kliknięcie – otwórz szczegóły (np. dialog lub browser)
        }

        binding.rvInvestments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInvestments.adapter = adapter

        lifecycleScope.launch {
            viewModel.investments.collect { items ->
                adapter.submitList(items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}